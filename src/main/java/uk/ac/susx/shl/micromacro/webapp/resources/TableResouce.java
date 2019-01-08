package uk.ac.susx.shl.micromacro.webapp.resources;

import com.google.gson.Gson;
import uk.ac.susx.shl.micromacro.db.Method52DAO;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;
import uk.ac.susx.tag.method51.core.meta.KeySet;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by sw206 on 18/07/2018.
 */
@Path("tables")
@Produces(MediaType.APPLICATION_JSON)
public class TableResouce {

    private final Method52DAO data;

    public TableResouce(Method52DAO data) {

        this.data = data;
    }


    @GET
    @Path("list")
    public Response listTables() throws SQLException {

        List<String> tables = data.listTables();

        return Response.status(Response.Status.OK).entity(
            tables
        ).build();

    }

    @GET
    @Path("schema")
    public Response listKeys(@QueryParam("table") String table) throws SQLException {

        Gson gson = GsonBuilderFactory.get().create();

        KeySet keys = data.schema(table);

        return Response.status(Response.Status.OK).entity(
                gson.toJson(keys)
        ).build();

    }
}
