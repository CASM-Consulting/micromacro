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


@Path("query/select")
@Produces(MediaType.APPLICATION_JSON)
public class SelectResource {

    private static final Logger LOG = Logger.getLogger(SelectResource.class.getName());

    private final QueryResource<Select, Update> selectResource;
    private final Method52DAO method52DAO;

    public SelectResource(DAO<String, Select> datumDAO, Method52DAO method52DAO) {
        selectResource = new QueryResource<>(datumDAO, method52DAO);
        this.method52DAO = method52DAO;
    }

    @POST
    @Path("query")
    public void select(@Suspended final AsyncResponse asyncResponse,
                           final Select select) throws Exception {
        selectResource.query(asyncResponse, select);
    }

    @POST
    @Path("cacheOnly")
    public void cacheOnly(@Suspended final AsyncResponse asyncResponse,
                       final Select select) throws Exception {
        selectResource.cacheOnly(asyncResponse, select);
    }

    @POST
    @Path("skipLimit")
    public void skipLimit(@Suspended final AsyncResponse asyncResponse,
                          @QueryParam("skip") Integer skip,
                          @QueryParam("limit") Integer limit,
                          final Select select) throws Exception {
        selectResource.skipLimit(asyncResponse, skip, limit, select);
    }

    @POST
    @Path("page")
    public void page(@Suspended final AsyncResponse asyncResponse,
                          @QueryParam("page") Integer page,
                          final Select select) throws Exception {
        selectResource.page(asyncResponse, page, select);
    }

    @POST
    @Path("partition")
    public void partition(@Suspended final AsyncResponse asyncResponse,
                     @QueryParam("partition") String partition,
                     final Select select) throws Exception {
        selectResource.partition(asyncResponse, partition, select);
    }



    @POST
    @Path("selectUpdate")
    public Response update(final Update update) {
        return selectResource.update(update);
    }

    @POST
    @Path("optimise")
    public Response optimist(Select select) {

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
