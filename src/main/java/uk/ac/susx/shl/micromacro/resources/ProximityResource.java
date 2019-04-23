package uk.ac.susx.shl.micromacro.resources;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.glassfish.jersey.media.multipart.FormDataParam;
import uk.ac.susx.shl.micromacro.jdbi.DAO;
import uk.ac.susx.shl.micromacro.jdbi.Method52DAO;
import uk.ac.susx.tag.method51.core.data.store2.query.Proximity;
import uk.ac.susx.tag.method51.core.data.store2.query.ProximityUpdate;
import uk.ac.susx.tag.method51.core.data.store2.query.Select;
import uk.ac.susx.tag.method51.core.data.store2.query.Update;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;


@Path("query/proximity")
@Produces(MediaType.APPLICATION_JSON)
public class ProximityResource {

    private static final Logger LOG = Logger.getLogger(ProximityResource.class.getName());

    private final QueryResource<Proximity, ProximityUpdate> resource;

    public ProximityResource(DAO<String, Proximity> datumDAO, Method52DAO method52DAO) {
        resource = new QueryResource<>(datumDAO, method52DAO);
    }

    @POST
    @Path("query")
    public void query(@Suspended final AsyncResponse asyncResponse,
                           final Proximity proximity) throws Exception {
        resource.query(asyncResponse, proximity);
    }

    @POST
    @Path("cacheOnly")
    public void cacheOnly(@Suspended final AsyncResponse asyncResponse,
                       final Proximity proximity) throws Exception {
        resource.cacheOnly(asyncResponse, proximity);
    }

    @POST
    @Path("skipLimit")
    public void skipLimit(@Suspended final AsyncResponse asyncResponse,
                          @QueryParam("skip") Integer skip,
                          @QueryParam("limit") Integer limit,
                          final Proximity proximity) throws Exception {
        resource.skipLimit(asyncResponse, skip, limit, proximity);
    }

    @POST
    @Path("page")
    public void page(@Suspended final AsyncResponse asyncResponse,
                          @QueryParam("page") Integer page,
                          final Proximity proximity) throws Exception {
        resource.page(asyncResponse, page, proximity);
    }

    @POST
    @Path("partition")
    public void partition(@Suspended final AsyncResponse asyncResponse,
                          @QueryParam("partition") String partition,
                          final Proximity proximity) throws Exception {
        resource.partition(asyncResponse, partition, proximity);
    }

    @POST
    @Path("proximityUpdate")
    public Response update(final ProximityUpdate update) {
        return resource.update(update);
    }


    @POST
    @Path("optimise")
    public Response optimise(Proximity proximity) {

        return resource.optimise(proximity);
    }


    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("counts")
    public Response counts(@FormDataParam("query") String proximity,
            @FormDataParam("partitionIds") String partitionIds) {
        Gson gson = GsonBuilderFactory.get().create();
        return resource.counts(gson.fromJson(proximity, Proximity.class), gson.fromJson(partitionIds, new TypeToken<List<String>>(){}.getType()));
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
