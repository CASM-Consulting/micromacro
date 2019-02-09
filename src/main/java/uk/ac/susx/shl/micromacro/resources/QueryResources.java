package uk.ac.susx.shl.micromacro.resources;


import uk.ac.susx.shl.micromacro.api.DatumRep;
import uk.ac.susx.shl.micromacro.api.ProxyRep;
import uk.ac.susx.shl.micromacro.api.SelectDistinctRep;
import uk.ac.susx.shl.micromacro.api.SelectRep;
import uk.ac.susx.shl.micromacro.core.QueryFactory;
import uk.ac.susx.shl.micromacro.core.QueryResultCache;
import uk.ac.susx.shl.micromacro.jdbi.DatumDAO;
import uk.ac.susx.tag.method51.core.data.store2.query.Proxy;
import uk.ac.susx.tag.method51.core.data.store2.query.Select;
import uk.ac.susx.tag.method51.core.meta.Datum;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Path("query")
@Produces(MediaType.APPLICATION_JSON)
public class QueryResources {

    private final QueryFactory queryFactory;
    private final DatumDAO datumDAO;
    private final QueryResultCache cache;

    public QueryResources(QueryFactory queryFactory, DatumDAO datumDAO, QueryResultCache cache) {
        this.queryFactory = queryFactory;
        this.datumDAO = datumDAO;
        this.cache = cache;
    }

    @POST
    @Path("select-distinct")
    public Response selectDistinct(SelectDistinctRep rep) throws SQLException {

        String sql = queryFactory.selectDistinct(rep).sql();

        List<String> data = datumDAO.selectString(sql);

        return Response.status(Response.Status.OK).entity(
                data
        ).build();
    }

    @POST
    @Path("select")
    public Response select(SelectRep rep) throws SQLException {

        Select select = queryFactory.select(rep);

        List<DatumRep> data = cache.cache(rep, () -> datumDAO.execute2Rep(select) );

        return Response.status(Response.Status.OK).entity(
                data
        ).build();
    }


    @POST
    @Path("proxy")
    public Response proxy(ProxyRep proxyRep) throws SQLException {

        Proxy proxy = queryFactory.proxy(proxyRep);

        List<DatumRep> data = cache.cache(proxyRep, () -> datumDAO.execute2Rep(proxy) );

        return Response.status(Response.Status.OK).entity(
            data
        ).build();
    }
}
