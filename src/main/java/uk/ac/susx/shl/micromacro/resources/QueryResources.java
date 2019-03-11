package uk.ac.susx.shl.micromacro.resources;


import uk.ac.susx.shl.micromacro.api.*;
import uk.ac.susx.shl.micromacro.core.QueryFactory;
import uk.ac.susx.shl.micromacro.core.QueryResultCache;
import uk.ac.susx.shl.micromacro.jdbi.DatumDAO;
import uk.ac.susx.tag.method51.core.data.store2.query.*;
import uk.ac.susx.tag.method51.core.meta.Datum;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Path("query")
@Produces(MediaType.APPLICATION_JSON)
public class QueryResources {

    private final DatumDAO datumDAO;
    private final QueryResultCache cache;

    public QueryResources(DatumDAO datumDAO, QueryResultCache cache) {
        this.datumDAO = datumDAO;
        this.cache = cache;
    }

    @POST
    @Path("optimise/proxy")
    public Response optimiseProxy(Proxy proxy) throws SQLException {

        datumDAO.optimiseTable(proxy);

        return Response.status(Response.Status.OK).entity(
                "OK"
        ).build();
    }

    @POST
    @Path("optimise/select")
    public Response optimiseSelect(Select select) throws SQLException {

        datumDAO.optimiseTable(select);

        return Response.status(Response.Status.OK).entity(
                "OK"
        ).build();
    }

    @POST
    @Path("select-distinct")
    public Response selectDistinct(SelectDistinct selectDistinct) throws SQLException {

        String sql = selectDistinct.sql();

        List<String> data = datumDAO.selectString(sql);

        return Response.status(Response.Status.OK).entity(
                data
        ).build();
    }

    @POST
    @Path("select")
    public void select(@Suspended final AsyncResponse asyncResponse,
                           @QueryParam("cacheOnly") @DefaultValue("false") Boolean cacheOnly,
                           @QueryParam("skip") Integer skip,
                           @QueryParam("limit") Integer limit,
                           final Select select) {

        Response response;
        QueryResultCache.CachedQueryResult<Select> cached = cache.cache(select, () -> datumDAO.execute2Rep(select) );

        if(cacheOnly) {
            response = Response.status(Response.Status.OK).entity( cached.count() ).build();
        } else if(skip != null && limit != null) {
            response = Response.status(Response.Status.OK).entity( cached.get(skip, skip+limit) ).build();
        } else {
            response = Response.status(Response.Status.OK).entity( StreamSupport.stream(cached.stream().spliterator(), false) ).build();
        }
        CompletableFuture<Response> promise = new CompletableFuture<>();
        promise.complete(response);
        promise.thenAccept(asyncResponse::resume).thenRun(cached::close);
    }


    @POST
    @Path("proxy")
    public void proxy(@Suspended final AsyncResponse asyncResponse,
                          @QueryParam("cacheOnly") @DefaultValue("false") Boolean cacheOnly,
                          @QueryParam("page") Integer page,
                          final Proxy proxy) throws SQLException {

        Response response;

        QueryResultCache.CachedQueryResult<Proxy> cached = cache.cache(proxy, () -> datumDAO.execute2Rep(proxy) , new QueryResultCache.ProxyPager());

        if(cacheOnly) {
            response = Response.status(Response.Status.OK).entity( cached.count() ).build();
        } else if(page != null) {
            int[] indices = cached.indices(page);

            response = Response.status(Response.Status.OK).entity( cached.get(indices[0], indices[1]) ).build();
        } else {
            response = Response.status(Response.Status.OK).entity( StreamSupport.stream(cached.stream().spliterator(), false) ).build();
        }

        CompletableFuture<Response> promise = new CompletableFuture<>();
        promise.complete(response);
        promise.thenAccept(asyncResponse::resume).thenRun(cached::close);
    }
}
