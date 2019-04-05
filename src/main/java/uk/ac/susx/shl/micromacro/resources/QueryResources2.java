package uk.ac.susx.shl.micromacro.resources;


import uk.ac.susx.shl.micromacro.core.QueryResultCache;
import uk.ac.susx.shl.micromacro.jdbi.CachingDAO;
import uk.ac.susx.shl.micromacro.jdbi.DAO;
import uk.ac.susx.tag.method51.core.data.store2.query.*;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;


@Path("query")
@Produces(MediaType.APPLICATION_JSON)
public class QueryResources2  extends BaseDAOResource<String, SqlQuery> {

    private static final Logger LOG = Logger.getLogger(QueryResultCache.class.getName());

    public QueryResources2(DAO<String, SqlQuery> datumDAO) {
        super(datumDAO);
    }

    @POST
    @Path("select")
    public void select(@Suspended final AsyncResponse asyncResponse,
                           @QueryParam("cacheOnly") @DefaultValue("false") Boolean cacheOnly,
                           @QueryParam("skip") Integer skip,
                           @QueryParam("limit") Integer limit,
                           final Select select) throws Exception {

        daoStreamResponse(asyncResponse, select, (stream-> {
            if (cacheOnly) {
                return stream.count();
            } else if (skip != null && limit != null) {
                List<?> list = datumDAO.list(select);
                return list.subList(Math.min(list.size(), skip), Math.min(list.size(), skip + limit)).stream();
            } else {
                return StreamSupport.stream(stream.spliterator(), false);
            }
        }));
    }

    @POST
    @Path("proximity")
    public void proxy(@Suspended final AsyncResponse asyncResponse,
                          @QueryParam("cacheOnly") @DefaultValue("false") Boolean cacheOnly,
                          @QueryParam("page") Integer page,
                          final Proximity proximity) throws Exception {
        daoStreamResponse(asyncResponse, proximity, (stream-> {
            if (cacheOnly) {
                return stream.count();
            } else if (page != null ) {

                CachingDAO<String, SqlQuery> cache = (CachingDAO<String, SqlQuery>)datumDAO.getDAO();

                int[] indices = cache.pageCache(cache.getQueryId(proximity)).get(page);

                List<?> list = datumDAO.list(proximity);

                return list.subList(Math.min(list.size(), indices[0]), Math.min(list.size(), indices[1])).stream();
            } else {
                return StreamSupport.stream(stream.spliterator(), false);
            }
        }));

    }
//
//    @POST
//    @Path("selectUpdate")
//    public void update(@Suspended final AsyncResponse asyncResponse,
//            final Update update) throws SQLException {
//
//        CompletableFuture<Object> promise = new CompletableFuture<>();
//
//        executorService.submit( (Runnable&CompletableFuture.AsynchronousCompletionTask)()-> {
//            try {
//                promise.complete(datumDAO.executeUpdate(update));
//            } catch (Exception ex) {
//                promise.completeExceptionally(ex);
//            }
//        });
//
//        promise.thenApply(result -> Response.status(Response.Status.OK).entity( result ).build())
//                .thenAccept(asyncResponse::resume)
//                .thenRun(() -> {
//                    datumDAO.addKey(update.table(), update.key());
//                })
//                .exceptionally(exception -> {
//                    LOG.warning(exception.getMessage());
//                    return null;
//                });
//
//    }
//
//    @POST
//    @Path("proxyUpdate")
//    public void proxyUpdate(@Suspended final AsyncResponse asyncResponse,
//                            final ProxyUpdate update) throws SQLException {
//
//        CompletableFuture<Object> promise = new CompletableFuture<>();
//
//        executorService.submit( (Runnable&CompletableFuture.AsynchronousCompletionTask)()-> {
//            try {
//                promise.complete(datumDAO.executeUpdate(update));
//            } catch (Exception ex) {
//                promise.completeExceptionally(ex);
//            }
//        });
//
//        promise.thenApply(result -> Response.status(Response.Status.OK).entity( result ).build())
//                .thenAccept(asyncResponse::resume)
//                .thenRun(() -> {
//                    datumDAO.addKey(update.table(), update.key());
//                })
//                .exceptionally(exception -> {
//                    LOG.warning(exception.getMessage());
//                    return null;
//                });
//    }


}
