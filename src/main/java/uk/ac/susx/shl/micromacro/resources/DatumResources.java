package uk.ac.susx.shl.micromacro.resources;


import com.google.common.collect.ImmutableList;
import uk.ac.susx.shl.micromacro.api.ProxyRep;
import uk.ac.susx.shl.micromacro.api.SelectDistinctRep;
import uk.ac.susx.shl.micromacro.api.SelectRep;
import uk.ac.susx.shl.micromacro.core.QueryFactory;
import uk.ac.susx.shl.micromacro.jdbi.DatumWrapper;
import uk.ac.susx.shl.micromacro.jdbi.DatumWrapperDAO;
import uk.ac.susx.tag.method51.core.data.store2.query.OrderBy;
import uk.ac.susx.tag.method51.core.data.store2.query.Proxy;
import uk.ac.susx.tag.method51.core.data.store2.query.Select;
import uk.ac.susx.tag.method51.core.data.store2.query.SelectDistinct;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.filters.DatumFilter;
import uk.ac.susx.tag.method51.core.meta.filters.KeyFilter;
import uk.ac.susx.tag.method51.core.meta.filters.KeyFilters;
import uk.ac.susx.tag.method51.core.meta.filters.logic.LogicParser;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("datum")
@Produces(MediaType.APPLICATION_JSON)
public class DatumResources {

    private final QueryFactory queryFactory;
    private final DatumWrapperDAO datumWrapperDAO;

    public DatumResources(QueryFactory queryFactory, DatumWrapperDAO datumWrapperDAO) {
        this.queryFactory = queryFactory;
        this.datumWrapperDAO = datumWrapperDAO;
    }

    @GET
    @Path("sql")
    public Response sql(@QueryParam("sql") String sql)  {

        List<DatumWrapper> data = datumWrapperDAO.select(sql);

        return Response.status(Response.Status.OK).entity(
                data
        ).build();
    }

    @POST
    @Path("select-distinct")
    public Response selectDistinct(SelectDistinctRep rep) throws SQLException {

        String sql = queryFactory.selectDistinct(rep).sql();

        List<String> data = datumWrapperDAO.selectString(sql);

        return Response.status(Response.Status.OK).entity(
                data
        ).build();
    }

    @POST
    @Path("select")
    public Response select(SelectRep rep) throws SQLException {

        Select select = queryFactory.select(rep);

        String sql = select.sql();

        List<DatumWrapper> data = datumWrapperDAO.select(sql);

        return Response.status(Response.Status.OK).entity(
                data
        ).build();
    }


    @POST
    @Path("proxy")
    public Response proxy(ProxyRep proxyRep) throws SQLException {
        Proxy proxy = queryFactory.proxy(proxyRep);

        String sql = proxy.sql();

        List<DatumWrapper> data = datumWrapperDAO.select(sql);

        return Response.status(Response.Status.OK).entity(
            data
        ).build();
    }

    private final static String KEY_NAME = "key";
    private final static String ARGS = "args";
    private final static String FILTER_NAME = "filter";

    private Map<String, KeyFilter> processLiterals(Map<String, Map<String, String>> specs, KeySet keys) {

        Map<String, KeyFilter> literals = new HashMap<>();

        for(Map.Entry<String, Map<String, String>> entry : specs.entrySet()) {
            Map<String, String> spec = entry.getValue();

            Key key = keys.get(spec.get(KEY_NAME));
            String name = spec.get(FILTER_NAME);
            String args = spec.get(ARGS);

            KeyFilter filter = KeyFilters.get(name, args, key);

            literals.put(entry.getKey(), filter);
        }

        return literals;
    }

    private OrderBy processOrderBy(String key) {
        return OrderBy.asc(Key.of(key, RuntimeType.ANY));
    }
}
