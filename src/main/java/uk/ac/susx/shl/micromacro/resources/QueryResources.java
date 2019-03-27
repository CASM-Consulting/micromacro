package uk.ac.susx.shl.micromacro.resources;



import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.MapMaker;
import uk.ac.susx.shl.micromacro.core.QueryResultCache;
import uk.ac.susx.shl.micromacro.jdbi.DatumDAO;
import uk.ac.susx.tag.method51.core.data.store2.query.*;
import uk.ac.susx.tag.method51.core.meta.Datum;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

@Path("query")
@Produces(MediaType.APPLICATION_JSON)
public class QueryResources {

    private static final Logger LOG = Logger.getLogger(QueryResultCache.class.getName());

    private final DatumDAO datumDAO;
    private final QueryResultCache cache;
    private final ExecutorService executorService;

    private final Cache<String, Lock> running;

    private static class LockCondition {
        private final ReentrantLock lock;
        private final Condition condition;
        private boolean running;

        public LockCondition() {
            lock = new ReentrantLock();
            condition = lock.newCondition();
            running = false;
        }

        public Lock lock() {
            return lock;
        }

        public Condition condition() {
            return condition;
        }

        public boolean running() {
            return running;
        }

        public void complete() {
            running = false;
        }
    }

    public QueryResources(DatumDAO datumDAO, QueryResultCache cache) {
        this.datumDAO = datumDAO;
        this.cache = cache;
        executorService = Executors.newFixedThreadPool(100);

        running = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    @POST
    @Path("optimise/proximity")
    public Response optimiseProxy(Proximity proxy) throws SQLException {

        datumDAO.optimiseTable(proxy);

        return Response.status(Response.Status.OK).entity(
                "OK"
        ).build();
    }

    @POST
    @Path("optimise/select")
    public Response optimiseSelect(Select select) throws SQLException {

        datumDAO.optimiseTable(select);

        return Response.status(Response.Status.OK).entity(
                "OK"
        ).build();
    }

    @POST
    @Path("select-distinct")
    public Response selectDistinct(SelectDistinct selectDistinct) throws SQLException {

        String sql = selectDistinct.sql();

        List<String> data = datumDAO.selectString(sql);

        return Response.status(Response.Status.OK).entity(
                data
        ).build();
    }

    @POST
    @Path("select")
    public void select(@Suspended final AsyncResponse asyncResponse,
                           @QueryParam("cacheOnly") @DefaultValue("false") Boolean cacheOnly,
                           @QueryParam("skip") Integer skip,
                           @QueryParam("limit") Integer limit,
                           final Select select) throws Exception {

//        final Lock lock = running.get(select.sql(), ReentrantLock::new);

//        lock.lock();

        try {

            QueryResultCache.CachedQueryResult<Select> cached;

            Function<String, Map> mapper = datumDAO.string2Map();

            if (select.partition() == null) {
                cached = cache.cache(select, () -> datumDAO.execute2String(select));
            } else {
                cached = cache.cache(select, () -> datumDAO.execute2String(select), new QueryResultCache.PartitionPager<>(mapper));
            }

            Supplier<Object> task;

            if (cacheOnly) {
                task = () -> cached.count();
            } else if (skip != null && limit != null) {
                task = () -> StreamSupport.stream(cached.get(skip, skip + limit).stream().map(mapper).spliterator(), false);
            } else {
                task = () -> StreamSupport.stream(cached.stream().map(mapper).spliterator(), false);
            }

            CompletableFuture
                    .supplyAsync(task, executorService)
                    .thenApply(result -> Response.status(Response.Status.OK).entity(result).build())
                    .thenAccept(asyncResponse::resume)
                    .exceptionally(exception -> {
                        LOG.warning(exception.getMessage());
                        cached.clear();
//                        lock.unlock();
                        return null;
                    })
                    .thenRun(cached::close)
//                    .thenRun(() -> lock.unlock())
            ;

        } catch (Exception e) {
//            lock.unlock();
        }
    }


    @POST
    @Path("proximity")
    public void proxy(@Suspended final AsyncResponse asyncResponse,
                          @QueryParam("cacheOnly") @DefaultValue("false") Boolean cacheOnly,
                          @QueryParam("page") Integer page,
                          final Proximity proximity) throws SQLException {

        Function<String, Map> mapper = datumDAO.string2Map();

        QueryResultCache.CachedQueryResult<Proximity> cached = cache.cache(proximity, () -> datumDAO.execute2String(proximity) , new QueryResultCache.PartitionPager<>(mapper));

        Supplier<Object> task;

        if(cacheOnly) {

            task = () -> cached.count();
        } else if(page != null) {
            int[] indices = cached.indices(page);

            task = () -> StreamSupport.stream(cached.get(indices[0], indices[1]).stream().map(mapper).spliterator(), false);
        } else {
            task = () -> StreamSupport.stream(cached.stream().map(mapper).spliterator(), false);
        }

        CompletableFuture
                .supplyAsync(task, executorService)
                .thenApply(result -> Response.status(Response.Status.OK).entity( result ).build())
                .thenAccept(asyncResponse::resume)
                .exceptionally(exception -> {
                    LOG.warning(exception.getMessage());
                    cached.clear();
                    return null;
                })
                .thenRun(cached::close);
    }

    @POST
    @Path("selectUpdate")
    public void update(@Suspended final AsyncResponse asyncResponse,
            final Update update) throws SQLException {

        CompletableFuture<Object> promise = new CompletableFuture<>();

        executorService.submit( (Runnable&CompletableFuture.AsynchronousCompletionTask)()-> {
            try {
                promise.complete(datumDAO.executeUpdate(update));
            } catch (Exception ex) {
                promise.completeExceptionally(ex);
            }
        });

        promise.thenApply(result -> Response.status(Response.Status.OK).entity( result ).build())
                .thenAccept(asyncResponse::resume)
                .thenRun(() -> {
                    datumDAO.addKey(update.table(), update.key());
                })
                .exceptionally(exception -> {
                    LOG.warning(exception.getMessage());
                    return null;
                });

    }

    @POST
    @Path("proxyUpdate")
    public void proxyUpdate(@Suspended final AsyncResponse asyncResponse,
                            final ProxyUpdate update) throws SQLException {

        CompletableFuture<Object> promise = new CompletableFuture<>();

        executorService.submit( (Runnable&CompletableFuture.AsynchronousCompletionTask)()-> {
            try {
                promise.complete(datumDAO.executeUpdate(update));
            } catch (Exception ex) {
                promise.completeExceptionally(ex);
            }
        });

        promise.thenApply(result -> Response.status(Response.Status.OK).entity( result ).build())
                .thenAccept(asyncResponse::resume)
                .thenRun(() -> {
                    datumDAO.addKey(update.table(), update.key());
                })
                .exceptionally(exception -> {
                    LOG.warning(exception.getMessage());
                    return null;
                });
    }


}
