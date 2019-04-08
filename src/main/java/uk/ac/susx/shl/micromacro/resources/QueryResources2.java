package uk.ac.susx.shl.micromacro.resources;


import uk.ac.susx.shl.micromacro.core.QueryResultCache;
import uk.ac.susx.shl.micromacro.jdbi.CachingDAO;
import uk.ac.susx.shl.micromacro.jdbi.DAO;
import uk.ac.susx.shl.micromacro.jdbi.Method52DAO;
import uk.ac.susx.shl.micromacro.jdbi.PartitionPager;
import uk.ac.susx.tag.method51.core.data.store2.query.*;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;


@Path("query")
@Produces(MediaType.APPLICATION_JSON)
public class QueryResources2  extends BaseDAOResource<String, SqlQuery> {

    private static final Logger LOG = Logger.getLogger(QueryResultCache.class.getName());

    private final Method52DAO method52DAO;

    public QueryResources2(DAO<String, SqlQuery> datumDAO, Method52DAO method52DAO) {
        super(datumDAO);
        this.method52DAO = method52DAO;
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

                int[] indices = cache.int2IntArr(cache.getQueryId(proximity), PartitionPager.ID2INTARR).get(page);

                List<?> list = datumDAO.list(proximity);

                return list.subList(Math.min(list.size(), indices[0]), Math.min(list.size(), indices[1])).stream();
            } else {
                return StreamSupport.stream(stream.spliterator(), false);
            }
        }));

    }

    @POST
    @Path("selectUpdate")
    public Response update(final Update update) {

        int n = datumDAO.update(update);

        method52DAO.addKey(update.table(), update.key());

        return Response.status(Response.Status.OK).entity( n ).build();
    }

    @POST
    @Path("proxyUpdate")
    public Response proxyUpdate(final ProxyUpdate update) {
        int n = datumDAO.update(update);

        method52DAO.addKey(update.table(), update.key());

        return Response.status(Response.Status.OK).entity( n ).build();
    }


    @POST
    @Path("optimise/proximity")
    public Response optimiseProxy(Proximity proxy) {

        method52DAO.optimiseTable(proxy);

        return Response.status(Response.Status.OK).entity(
                "OK"
        ).build();
    }

    @POST
    @Path("optimise/select")
    public Response optimiseSelect(Select select) {

        method52DAO.optimiseTable(select);

        return Response.status(Response.Status.OK).entity(
                "OK"
        ).build();
    }

//    @POST
//    @Path("select-distinct")
//    public Response selectDistinct(SelectDistinct selectDistinct) throws SQLException {
//
//        String sql = selectDistinct.sql();
//
//        List<String> data = datumDAO.selectString(sql);
//
//        return Response.status(Response.Status.OK).entity(
//                data
//        ).build();
//    }


}
