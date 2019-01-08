package uk.ac.susx.shl.micromacro.webapp.resources;


import io.dropwizard.hibernate.UnitOfWork;
import uk.ac.susx.shl.micromacro.db.DatumWrapper;
import uk.ac.susx.shl.micromacro.db.DatumWrapperDAO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Path("datum")
@Produces(MediaType.APPLICATION_JSON)
public class DatumResources {

    private final DatumWrapperDAO datumWrapperDAO;

    public DatumResources(DatumWrapperDAO datumWrapperDAO) {
        this.datumWrapperDAO = datumWrapperDAO;
    }

    @GET
    @Path("sql")
    @UnitOfWork
    public Response sql(@QueryParam("sql") String sql) throws SQLException {

        List<DatumWrapper> data = datumWrapperDAO.execute(sql);

        return Response.status(Response.Status.OK).entity(
                data
        ).build();
    }

    @GET
    @Path("query")
    @UnitOfWork
    public Response query(@QueryParam("query") String sql) throws SQLException {

        List<DatumWrapper> data = datumWrapperDAO.execute(sql);

        return Response.status(Response.Status.OK).entity(
                data
        ).build();
    }

}
