package uk.ac.susx.shl.micromacro.resources;

import uk.ac.susx.shl.micromacro.core.QueryResultCache;
import uk.ac.susx.shl.micromacro.jdbi.DAO;
import uk.ac.susx.shl.micromacro.jdbi.PartitionPager;
import uk.ac.susx.tag.method51.core.data.store2.query.Partitioner;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlQuery;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
public class BaseDAOResource<T, Q extends SqlQuery> {

    private static final Logger LOG = Logger.getLogger(QueryResultCache.class.getName());

    protected final DAO<T, Q> datumDAO;
    private final ExecutorService executorService;

    protected BaseDAOResource(DAO<T, Q> datumDAO) {
        this.datumDAO = datumDAO;
        executorService = Executors.newFixedThreadPool(100);
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
        try {

            List<BiFunction> functions = new ArrayList<>();
            if(query instanceof Partitioner) {

                Partitioner partitioner = (Partitioner)query;

                functions.add(new PartitionPager(partitioner.partition().key().toString()));
            }

            Supplier<Object> task = ()-> {
                Stream<T> stream = datumDAO.stream(query, functions.toArray(new BiFunction[]{}));
                streamRef.set(stream);
                return handler.apply(stream);
            };

            CompletableFuture
                    .supplyAsync(task, executorService)
                    .thenApply(result -> Response.status(Response.Status.OK).entity(result).build())
                    .whenComplete( (r,e) -> {
                        if(e != null) {
                            LOG.warning(e.getMessage());
                            asyncResponse.resume(e);
                        } else {
                            asyncResponse.resume(r);
                        }
                        if(streamRef.get()!=null) {
                            streamRef.get().close();
                        }
                        complete.set(true);
                    }).get(); //must block

        } finally {
            if(streamRef.get()!=null && !complete.get()) {
                streamRef.get().close();
            }
        }
    }
}
