package uk.ac.susx.shl.micromacro.resources;


import uk.ac.susx.shl.micromacro.jdbi.DAO;
import uk.ac.susx.shl.micromacro.jdbi.Method52DAO;
import uk.ac.susx.tag.method51.core.data.store2.query.*;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;


@Path("query/select")
@Produces(MediaType.APPLICATION_JSON)
public class SelectResource {

    private static final Logger LOG = Logger.getLogger(SelectResource.class.getName());

    private final QueryResource<Select, Update> resource;

    public SelectResource(DAO<String, Select> datumDAO, Method52DAO method52DAO) {
        resource = new QueryResource<>(datumDAO, method52DAO);
    }

    @POST
    @Path("query")
    public void select(@Suspended final AsyncResponse asyncResponse,
                           final Select select) throws Exception {
        resource.query(asyncResponse, select);
    }

    @POST
    @Path("cacheOnly")
    public void cacheOnly(@Suspended final AsyncResponse asyncResponse,
                       final Select select) throws Exception {
        resource.cacheOnly(asyncResponse, select);
    }

    @POST
    @Path("skipLimit")
    public void skipLimit(@Suspended final AsyncResponse asyncResponse,
                          @QueryParam("skip") Integer skip,
                          @QueryParam("limit") Integer limit,
                          final Select select) throws Exception {
        resource.skipLimit(asyncResponse, skip, limit, select);
    }

    @POST
    @Path("page")
    public void page(@Suspended final AsyncResponse asyncResponse,
                          @QueryParam("page") Integer page,
                          final Select select) throws Exception {
        resource.page(asyncResponse, page, select);
    }

    @POST
    @Path("partition")
    public void partition(@Suspended final AsyncResponse asyncResponse,
                     @QueryParam("partition") String partition,
                     final Select select) throws Exception {
        resource.partition(asyncResponse, partition, select);
    }



    @POST
    @Path("selectUpdate")
    public Response update(final Update update) {
        return resource.update(update);
    }

    @POST
    @Path("optimise")
    public Response optimise(Select select) {

        return resource.optimise(select);
    }

    @POST
    @Path("counts")
    public Response counts(Select select,
             @QueryParam("partitionIds") List<String> partitionIds) {

        return resource.counts(select, partitionIds);
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
