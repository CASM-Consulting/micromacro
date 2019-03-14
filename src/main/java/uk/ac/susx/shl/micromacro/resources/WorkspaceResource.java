package uk.ac.susx.shl.micromacro.resources;


import uk.ac.susx.shl.micromacro.core.*;
import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;
import uk.ac.susx.tag.method51.core.data.store2.query.Proxy;
import uk.ac.susx.tag.method51.core.data.store2.query.Select;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.Map;

@Path("workspace")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WorkspaceResource {

    private final Workspaces workspaces;
    private final QueryFactory queryFactory;
    private final QueryResultCache cache;

    public WorkspaceResource(Workspaces workspaces, QueryFactory queryFactory, QueryResultCache cache) {
        this.workspaces = workspaces;
        this.queryFactory = queryFactory;
        this.cache = cache;
    }


    @GET
    @Path("getQueryMeta")
    public Response getQueryMeta(@QueryParam("workspaceId") String workspaceId,
                                 @QueryParam("queryId") String queryId,
                                 @QueryParam("metaId") String metaId
    ) {
        Workspace workspace = workspaces.get(workspaceId);

        Query query = workspace.queries().get(queryId);

        return Response.ok().entity(
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

        return Response.ok().entity(
            query.getMeta().get(metaId)
        ).build();
    }

    @GET
    @Path("loadQuery")
    public Response loadQuery(@QueryParam("workspaceId") String workspaceId,
                             @QueryParam("queryId") String queryId,
                             @QueryParam("ver") int ver
    ) {

        Workspace workspace = workspaces.get(workspaceId);

        Query query = workspace.queries().get(queryId);

        Map rep = queryFactory.rep(query.get(ver));

        boolean isCached = cache.isCached(query.get(ver));

        rep.put("isCached", isCached);

        return Response.ok().entity(
            rep
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

        return Response.ok().entity(
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

        return Response.ok().entity(
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

        return Response.ok().entity(
                queryFactory.rep(workspaces.get(workspaceId).queries().get(queryId).get())
        ).build();
    }


    @GET
    @Path("clearCache")
    public <T extends DatumQuery> Response clearCache(@QueryParam("workspaceId") String workspaceId,
                                                     @QueryParam("queryId") String queryId) {

        Workspace workspace = workspaces.get(workspaceId);

        Query<T> query = workspace.getQuery(queryId);

        for(T q : query.history()) {
            cache.clearCache(q);
        }

        return Response.ok().build();
    }

    @GET
    @Path("clearCacheAll")
    public <T extends DatumQuery> Response clearCacheAll(@QueryParam("workspaceId") String workspaceId) {

        Workspace workspace = workspaces.get(workspaceId);

        for(Query<T> query : workspace.queries().values() ) {
            for(T q : query.history()) {
                cache.clearCache(q);
            }
        }

        return Response.ok().build();
    }
}
