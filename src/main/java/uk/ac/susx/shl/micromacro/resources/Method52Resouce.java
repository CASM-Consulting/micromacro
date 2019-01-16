package uk.ac.susx.shl.micromacro.resources;

import com.google.gson.Gson;
import uk.ac.susx.shl.micromacro.jdbi.Method52DAO;
import uk.ac.susx.tag.method51.core.data.StoreException;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.KeySet;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by sw206 on 18/07/2018.
 */
@Path("m52")
@Produces(MediaType.APPLICATION_JSON)
public class Method52Resouce {

    private final Method52DAO data;

    public Method52Resouce(Method52DAO data) {

        this.data = data;
    }


    @GET
    @Path("list-tables")
    public Response listTables() throws SQLException {

        List<String> tables = data.listTables();

        return Response.status(Response.Status.OK).entity(
            tables
        ).build();

    }

    @GET
    @Path("list-keys")
    public Response listKeys(@QueryParam("table") String table) throws SQLException {

        Gson gson = GsonBuilderFactory.get().create();

        KeySet keys = data.schema(table);

        return Response.status(Response.Status.OK).entity(
                gson.toJson(keys)
        ).build();

    }

    @POST
    @Path("get-annotations")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getScores(@QueryParam("table") String table,
                              @QueryParam("trialIdKey") String trialIdKey,
                              @QueryParam("sentenceIdKey") String sentenceIdKey,
                              @QueryParam("annotationKeys") List<String> annotationKeys,
                              List<String> ids) throws SQLException, StoreException {

        Gson gson = GsonBuilderFactory.get().create();

        List<Datum> results = data.getScores(table, trialIdKey, sentenceIdKey, annotationKeys, ids);

        return Response.status(Response.Status.OK).entity(
                gson.toJson(results)
        ).build();
    }

}
