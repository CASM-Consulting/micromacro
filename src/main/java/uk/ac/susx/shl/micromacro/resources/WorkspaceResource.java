package uk.ac.susx.shl.micromacro.resources;


import uk.ac.susx.shl.micromacro.core.*;
import uk.ac.susx.tag.method51.core.data.store2.query.Proxy;
import uk.ac.susx.tag.method51.core.data.store2.query.Select;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

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
    @Path("getQueryMeta")
    public Response getQueryMeta(@QueryParam("workspaceId") String workspaceId,
                                 @QueryParam("queryId") String queryId,
                                 @QueryParam("metaId") String metaId
    ) {
        Workspace workspace = workspaces.get(workspaceId);

        Query query = workspace.queries().get(queryId);

        return Response.status(Response.Status.OK).entity(
                query.getMeta().get(metaId)
        ).build();
    }

    @POST
    @Path("setQueryMeta")
    public Response setQueryMeta(@QueryParam("workspaceId") String workspaceId,
                            @QueryParam("queryId") String queryId,
                            @QueryParam("metaId") String metaId,
                            String data
                            ) {
        Workspace workspace = workspaces.get(workspaceId);

        Query query = workspace.queries().get(queryId);

        query.setMeta(metaId, data);

        workspaces.save(workspace);

        return Response.status(Response.Status.OK).entity(
            query.getMeta().get(metaId)
        ).build();
    }

    @GET
    @Path("loadQuery")
    public Response loadQuery(@QueryParam("workspaceId") String workspaceId,
                             @QueryParam("queryId") String queryId,
                             @QueryParam("ver") int ver
    ) throws SQLException {

        Workspace workspace = workspaces.get(workspaceId);

        Query query = workspace.queries().get(queryId);

        return Response.status(Response.Status.OK).entity(
                queryFactory.rep(query.get(ver))
        ).build();
    }

    @GET
    @Path("getQueryKeys")
    public Response getQueryKeys(@QueryParam("workspaceId") String workspaceId,
                                 @QueryParam("queryId") String queryId,
                                 @QueryParam("ver") int ver
    ) {
        Workspace workspace = workspaces.get(workspaceId);

        Query query = workspace.queries().get(queryId);

        return Response.status(Response.Status.OK).entity(
                queryFactory.keys(query.get(ver).whereKeys())
        ).build();
    }

    @POST
    @Path("addProxy")
    public Response addProxy(@QueryParam("workspaceId") String workspaceId,
                             @QueryParam("queryId") String queryId,
                             Proxy proxy) throws SQLException {

        Workspace workspace = workspaces.get(workspaceId);

        workspace.add(queryId, proxy);

        workspaces.save(workspace);

        return Response.status(Response.Status.OK).entity(
                queryFactory.rep(workspaces.get(workspaceId).queries().get(queryId).get())
        ).build();
    }

    @POST
    @Path("addSelect")
    public Response addSelect(@QueryParam("workspaceId") String workspaceId,
                             @QueryParam("queryId") String queryId,
                             Select select) throws SQLException {

        Workspace workspace = workspaces.get(workspaceId);

        workspace.add(queryId, select);

        workspaces.save(workspace);

        return Response.status(Response.Status.OK).entity(
                queryFactory.rep(workspaces.get(workspaceId).queries().get(queryId).get())
        ).build();
    }

}
