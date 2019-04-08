package uk.ac.susx.shl.micromacro.resources;


import uk.ac.susx.shl.micromacro.jdbi.DAO;
import uk.ac.susx.shl.micromacro.jdbi.Method52DAO;
import uk.ac.susx.tag.method51.core.data.store2.query.Proximity;
import uk.ac.susx.tag.method51.core.data.store2.query.ProxyUpdate;
import uk.ac.susx.tag.method51.core.data.store2.query.Select;
import uk.ac.susx.tag.method51.core.data.store2.query.Update;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;


@Path("query/proximity")
@Produces(MediaType.APPLICATION_JSON)
public class ProximityResource {

    private static final Logger LOG = Logger.getLogger(ProximityResource.class.getName());

    private final QueryResource<Proximity, ProxyUpdate> proximityResource;
    private final Method52DAO method52DAO;

    public ProximityResource(DAO<String, Proximity> datumDAO, Method52DAO method52DAO) {
        proximityResource = new QueryResource<>(datumDAO, method52DAO);
        this.method52DAO = method52DAO;
    }

    @POST
    @Path("query")
    public void select(@Suspended final AsyncResponse asyncResponse,
                           final Proximity proximity) throws Exception {
        proximityResource.query(asyncResponse, proximity);
    }

    @POST
    @Path("cacheOnly")
    public void cacheOnly(@Suspended final AsyncResponse asyncResponse,
                       final Proximity proximity) throws Exception {
        proximityResource.cacheOnly(asyncResponse, proximity);
    }

    @POST
    @Path("skipLimit")
    public void skipLimit(@Suspended final AsyncResponse asyncResponse,
                          @QueryParam("skip") Integer skip,
                          @QueryParam("limit") Integer limit,
                          final Proximity proximity) throws Exception {
        proximityResource.skipLimit(asyncResponse, skip, limit, proximity);
    }

    @POST
    @Path("page")
    public void proxy(@Suspended final AsyncResponse asyncResponse,
                          @QueryParam("page") Integer page,
                          final Proximity proximity) throws Exception {
        proximityResource.page(asyncResponse, page, proximity);
    }

    @POST
    @Path("partition")
    public void partition(@Suspended final AsyncResponse asyncResponse,
                          @QueryParam("partition") String partition,
                          final Proximity proximity) throws Exception {
        proximityResource.partition(asyncResponse, partition, proximity);
    }

    @POST
    @Path("proximityUpdate")
    public Response update(final Update update) {
        return proximityResource.update(update);
    }

    @POST
    @Path("optimise")
    public Response optimist(Proximity proximity) {

        method52DAO.optimiseTable(proximity);

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
