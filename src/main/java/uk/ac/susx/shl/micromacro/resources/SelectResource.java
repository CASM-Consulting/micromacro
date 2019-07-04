package uk.ac.susx.shl.micromacro.resources;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.glassfish.jersey.media.multipart.FormDataParam;
import uk.ac.susx.shl.micromacro.jdbi.DAO;
import uk.ac.susx.shl.micromacro.jdbi.Method52DAO;
import uk.ac.susx.tag.method51.core.data.store2.query.*;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;

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
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("partitions")
    public Response partitoins(@FormDataParam("query") String select,
                               @FormDataParam("partitionIds") String partitionIds) {
        Gson gson = GsonBuilderFactory.get().create();
        return resource.partitions(gson.fromJson(select, Select.class), gson.fromJson(partitionIds, new TypeToken<List<String>>(){}.getType()));
    }

    @POST
    @Path("update")
    public Response update(final Update update) {
        return resource.update(update);
    }

    @POST
    @Path("optimise")
    public Response optimise(Select select) {

        return resource.optimise(select);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("counts")
    public Response counts(@FormDataParam("query") String select,
                           @FormDataParam("partitionIds") String partitionIds) {
        Gson gson = GsonBuilderFactory.get().create();
        return resource.counts(gson.fromJson(select, Select.class), gson.fromJson(partitionIds, new TypeToken<List<String>>(){}.getType()));
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
