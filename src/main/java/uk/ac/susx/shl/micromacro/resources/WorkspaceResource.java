package uk.ac.susx.shl.micromacro.resources;


import uk.ac.susx.shl.micromacro.api.ProxyRep;
import uk.ac.susx.shl.micromacro.api.WorkspaceRep;
import uk.ac.susx.shl.micromacro.core.Query;
import uk.ac.susx.shl.micromacro.core.QueryFactory;
import uk.ac.susx.shl.micromacro.core.Workspace;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Path("workspaces")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WorkspaceResource {

    private final Map<String, Workspace> workspaces;
    private final QueryFactory queryFactory;

    public WorkspaceResource(Map<String, Workspace> workspaces, QueryFactory queryFactory) {
        this.workspaces = workspaces;
        this.queryFactory = queryFactory;
    }

    @POST
    @Path("addQuery")
    public Response addQuery(@QueryParam("workspace") String workspaceName,
                             @QueryParam("queryName") String queryName,
                             ProxyRep query) throws SQLException {

        Workspace workspace = workspaces.get(workspaceName);

        workspace.add(queryName, queryFactory.proxy(query));

        return Response.status(Response.Status.OK).entity(
                workspace
        ).build();
    }

    @GET
    @Path("loadQuery")
    public Response loadQuery(@QueryParam("workspace") String workspaceName,
                              @QueryParam("queryName") String queryName) {

        Query query = workspaces.get(workspaceName).queries().get(queryName);

        return Response.status(Response.Status.OK).entity(
            queryFactory.rep(query.get())
        ).build();
    }

    @GET
    @Path("listQueries")
    public Response list(@QueryParam("name") String name) {

        List<String> queries = new ArrayList<>(workspaces.get(name).queries().keySet());

        Collections.sort(queries);

        return Response.status(Response.Status.OK).entity(
                queries
        ).build();
    }

    @GET
    @Path("load")
    public Response load(@QueryParam("name") String name) {

        Workspace workspace = workspaces.get(name);

        return Response.status(Response.Status.OK).entity(
                new WorkspaceRep(workspace)
        ).build();
    }

    @GET
    @Path("save")
    public Response save(@QueryParam("name") String name) {

        Workspace workspace = workspaces.get(name);

        workspaces.put(name, workspace);

        return Response.status(Response.Status.OK).entity(
            new WorkspaceRep(workspace)
        ).build();
    }

    @GET
    @Path("create")
    public Response create(@QueryParam("name") String name) throws SQLException {

        Workspace workspace = new Workspace(name);

        workspaces.put(name, workspace);

        return Response.status(Response.Status.OK).entity(
                new WorkspaceRep(workspace)
        ).build();
    }

    @GET
    @Path("list")
    public Response list() throws SQLException {

        List<String> workspacesNames = new ArrayList<>(workspaces.keySet());
        Collections.sort(workspacesNames);

        return Response.status(Response.Status.OK).entity(
                workspacesNames
        ).build();
    }

}
