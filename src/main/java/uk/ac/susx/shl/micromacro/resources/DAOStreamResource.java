package uk.ac.susx.shl.micromacro.resources;

import uk.ac.susx.shl.micromacro.jdbi.DAO;
import uk.ac.susx.shl.micromacro.jdbi.PartitionedPager;
import uk.ac.susx.tag.method51.core.data.store2.query.Partitioned;
import uk.ac.susx.tag.method51.core.data.store2.query.Scoped;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlQuery;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;



/**
 * Where request handlers utilise an AsyncResponse they must manage the life-cycle of the DAO response stream such that
 * the stream is closed after the response data is supplied. Regardless of whether the stream is consumed, it must be
 * closed. This class supplies the necessary stream life-cycled handling such that a simple handler method can provide
 * the required functionality for a given request.
 *
 */
public class DAOStreamResource<T, Q extends SqlQuery> {

    private static final Logger LOG = Logger.getLogger(DAOStreamResource.class.getName());

    protected final DAO<T, Q> datumDAO;
    private final ExecutorService executorService;

    protected DAOStreamResource(DAO<T, Q> datumDAO) {
        this.datumDAO = datumDAO;
        executorService = Executors.newFixedThreadPool(100);
    }

    private boolean isPartitioned(Q query) {
        return query instanceof Partitioned && ((Partitioned) query).partition() != null;
    }

    private boolean isScoped(Q query) {
        return query instanceof Scoped && ((Scoped) query).scope() != null;
    }

    private boolean isScopedAndPartition(Q query) {
        return isPartitioned(query) && isScoped(query);
    }

    private boolean isScopedOrPartition(Q query) {
        return isPartitioned(query) || isScoped(query);
    }

    /**
     * The intended use of AsyncResponse is to release the handling thread to the web framework for a long running
     * compute; however, it's being used here to allow for a callback after the stream has been consumed by the
     * framework.
     * @param asyncResponse
     * @param query
     * @param handler
     * @throws Exception
     */
    public void daoStreamResponse(final AsyncResponse asyncResponse, Q query, Function<Stream<T>, Object> handler) throws Exception {
        AtomicReference<Stream<T>> streamRef = new AtomicReference<>();
        AtomicBoolean complete = new AtomicBoolean(false);

        Supplier<Void> handle = () -> {
            try {

                List<BiFunction> functions = new ArrayList<>();

                if(isScopedAndPartition(query) || isPartitioned(query)) {
                    Partitioned partitioned = (Partitioned) query;

                    functions.add(new PartitionedPager(partitioned.partition().key().toString()));
                } else if(isScoped(query)) {
                    Scoped scoped = (Scoped) query;

                    functions.add(new PartitionedPager(scoped.scope().key().toString()));
                }

                Supplier<Object> task = () -> {
                    Stream<T> stream = datumDAO.stream(query, functions.toArray(new BiFunction[]{}));
                    streamRef.set(stream);
                    return handler.apply(stream);
                };

                CompletableFuture
                        .supplyAsync(task, executorService)
                        .thenApply(result -> Response.status(Response.Status.OK).entity(result).build())
                        .whenComplete((r, e) -> {
                            if (e != null) {
                                LOG.warning(e.getMessage());
                                asyncResponse.resume(e);
                            } else {
                                asyncResponse.resume(r);
                            }
                            if (streamRef.get() != null) {
                                streamRef.get().close();
                            }
                            complete.set(true);
                        }).get(); //must block
            } catch (InterruptedException | ExecutionException e) {
                if (streamRef.get() != null) {
                    streamRef.get().close();
                }
            } finally {
                if(streamRef.get()!=null && !complete.get()) {
                    streamRef.get().close();
                }
            }
            return null;
        };
        CompletableFuture
                .supplyAsync(handle, executorService)
                .exceptionally(e -> {
                    if(streamRef.get()!=null && !complete.get()) {
                        streamRef.get().close();
                    }
                    return null;
                });
    }
}
