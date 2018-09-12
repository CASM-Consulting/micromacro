package uk.ac.susx.shl.micromacro.webapp.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.dropwizard.jersey.params.LocalDateParam;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import uk.ac.susx.shl.micromacro.client.Method52Data;
import uk.ac.susx.shl.micromacro.core.data.text.SimpleDocument;
import uk.ac.susx.shl.micromacro.db.JdbiProvider;
import uk.ac.susx.tag.method51.core.data.PostgreSQLConnection;
import uk.ac.susx.tag.method51.core.data.PostgresUtils;
import uk.ac.susx.tag.method51.core.data.StoreException;
import uk.ac.susx.tag.method51.core.data.impl.PostgreSQLDatumStore;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

/**
 * Created by sw206 on 18/07/2018.
 */
@Path("m52")
@Produces(MediaType.APPLICATION_JSON)
public class Method52Resouce {

    private final Method52Data data;

    public Method52Resouce(Method52Data data) {

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

        KeySet keys = data.listKeys(table);

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
