package uk.ac.susx.shl.micromacro.resources;


import com.google.common.collect.ImmutableMap;
import uk.ac.susx.shl.micromacro.core.*;
import uk.ac.susx.shl.micromacro.jdbi.CachingDAO;
import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;
import uk.ac.susx.tag.method51.core.data.store2.query.Proximity;
import uk.ac.susx.tag.method51.core.data.store2.query.Select;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlQuery;
import uk.ac.susx.tag.method51.core.meta.filters.KeyFilter;

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
    private final CachingDAO<String, SqlQuery> cachingDAO;

    public WorkspaceResource(Workspaces workspaces, QueryFactory queryFactory, CachingDAO<String, SqlQuery> cachingDAO) {
        this.workspaces = workspaces;
        this.queryFactory = queryFactory;
        this.cachingDAO = cachingDAO;
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
    @Path("loadMap")
    public Response loadMap(@QueryParam("workspaceId") String workspaceId,
                              @QueryParam("mapId") String mapId
    ) {
        Workspace workspace = workspaces.get(workspaceId);

        GeoMap map = workspace.getMap(mapId);

        return Response.ok().entity(
                map
        ).build();
    }

    @POST
    @Path("saveMap")
    public Response addMap(@QueryParam("workspaceId") String workspaceId,
                                 @QueryParam("mapId") String mapId,
                                 GeoMap map) {
        Workspace workspace = workspaces.get(workspaceId);

        workspace.addMap(mapId, map);

        workspaces.save(workspace);

        return Response.ok().entity(
                workspaces.get(workspaceId).getMap(mapId)
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
    @Path("addProximity")
    public Response addProximity(@QueryParam("workspaceId") String workspaceId,
                             @QueryParam("queryId") String queryId,
                             Proximity proximity) throws SQLException {

        Workspace workspace = workspaces.get(workspaceId);

        workspace.addQuery(queryId, proximity);

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

        workspace.addQuery(queryId, select);

        workspaces.save(workspace);

        return Response.ok().entity(
                queryFactory.rep(select)
        ).build();
    }

    @GET
    @Path("deleteQuery")
    public Response deleteQuery(@QueryParam("workspaceId") String workspaceId,
                              @QueryParam("queryId") String queryId) throws SQLException {

        Workspace workspace = workspaces.get(workspaceId);

        workspace.deleteQuery(queryId);

        workspaces.save(workspace);

        return Response.ok().entity(
            ImmutableMap.of("message", "ok")
        ).build();
    }

    @GET
    @Path("clearCache")
    public <T extends DatumQuery> Response clearCache(@QueryParam("workspaceId") String workspaceId,
                                                     @QueryParam("queryId") String queryId) {

        Workspace workspace = workspaces.get(workspaceId);

        Query<T> query = workspace.getQuery(queryId);

        for(T q : query.history()) {
            cachingDAO.clearCache(q);
        }

        return Response.ok().build();
    }

    @GET
    @Path("clearCacheAll")
    public <T extends DatumQuery> Response clearCacheAll(@QueryParam("workspaceId") String workspaceId) {

        Workspace workspace = workspaces.get(workspaceId);

        for(Query<T> query : workspace.queries().values() ) {
            for(T q : query.history()) {
                cachingDAO.clearCache(q);
            }
        }

        return Response.ok().build();
    }

    @POST
    @Path("setTableLiterals")
    public Response setTableLiterals(@QueryParam("workspaceId") String workspaceId,
                              @QueryParam("table") String table,
                              Map<String, KeyFilter> literals) throws SQLException {

        Workspace workspace = workspaces.get(workspaceId);

        workspace.tableLiterals(table, literals);

        workspaces.save(workspace);

        return Response.ok().entity(
                workspaces.get(workspaceId).tableLiterals(table)
        ).build();
    }
}
