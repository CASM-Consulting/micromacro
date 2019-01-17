package uk.ac.susx.shl.micromacro.resources;


import uk.ac.susx.shl.micromacro.api.ProxyRep;
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
import java.util.stream.Collectors;

@Path("workspaces")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WorkspacesResource {

    private final Workspaces workspaces;
    private final WorkspaceFactory workspaceFactory;

    public WorkspacesResource(Workspaces workspaces, WorkspaceFactory workspaceFactory) {
        this.workspaces = workspaces;
        this.workspaceFactory = workspaceFactory;
    }


    @GET
    @Path("create")
    public Response create(@QueryParam("name") String name)  {

        Workspace workspace = workspaces.create(name);

        return Response.status(Response.Status.OK).entity(
                workspaceFactory.rep(workspace)
        ).build();
    }

    @GET
    @Path("list")
    public Response list() throws SQLException {

        List<WorkspaceRep> workspacesNames = workspaces.get().values()
                .stream()
                .map(w->workspaceFactory.rep(w))
                .collect(Collectors.toList());

        Collections.sort(workspacesNames, (o1, o2) -> o1.name.compareToIgnoreCase(o2.name));

        return Response.status(Response.Status.OK).entity(
                workspacesNames
        ).build();
    }

}
