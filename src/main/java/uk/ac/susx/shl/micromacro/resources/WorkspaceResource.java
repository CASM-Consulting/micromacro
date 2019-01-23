package uk.ac.susx.shl.micromacro.resources;


import uk.ac.susx.shl.micromacro.api.AbstractQueryRep;
import uk.ac.susx.shl.micromacro.api.ProxyRep;
import uk.ac.susx.shl.micromacro.api.SelectRep;
import uk.ac.susx.shl.micromacro.api.WorkspaceRep;
import uk.ac.susx.shl.micromacro.core.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Path("workspace")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WorkspaceResource {

    private final Workspaces workspaces;
    private final QueryFactory queryFactory;

    public WorkspaceResource(Workspaces workspaces, QueryFactory queryFactory) {
        this.workspaces = workspaces;
        this.queryFactory = queryFactory;
    }


    @GET
    @Path("loadQuery")
    public Response loadQuery(@QueryParam("workspace") String workspaceName,
                             @QueryParam("queryName") String queryName,
                             @QueryParam("ver") int ver
    ) throws SQLException {

        Workspace workspace = workspaces.get(workspaceName);

        Query query = workspace.queries().get(queryName);

        return Response.status(Response.Status.OK).entity(
                queryFactory.rep(query.get(ver))
        ).build();
    }

    @POST
    @Path("addProxy")
    public Response addProxy(@QueryParam("workspaceName") String workspaceName,
                             @QueryParam("queryName") String queryName,
                             ProxyRep query) throws SQLException {

        Workspace workspace = workspaces.get(workspaceName);

        workspace.add(queryName, queryFactory.proxy(query));

        workspaces.save(workspace);

        return Response.status(Response.Status.OK).entity(
                queryFactory.rep(workspaces.get(workspaceName).queries().get(queryName).get())
        ).build();
    }

    @POST
    @Path("addSelect")
    public Response addSelect(@QueryParam("workspaceName") String workspaceName,
                             @QueryParam("queryName") String queryName,
                             SelectRep query) throws SQLException {

        Workspace workspace = workspaces.get(workspaceName);

        workspace.add(queryName, queryFactory.select(query));

        workspaces.save(workspace);

        return Response.status(Response.Status.OK).entity(
                queryFactory.rep(workspaces.get(workspaceName).queries().get(queryName).get())
        ).build();
    }

}
