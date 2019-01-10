package uk.ac.susx.shl.micromacro.webapp.resources;


import com.google.common.collect.ImmutableList;
import io.dropwizard.hibernate.UnitOfWork;
import uk.ac.susx.shl.micromacro.db.DatumWrapper;
import uk.ac.susx.shl.micromacro.db.DatumWrapperDAO;
import uk.ac.susx.shl.micromacro.db.Method52DAO;
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

    private final DatumWrapperDAO datumWrapperDAO;
    private final Method52DAO method52DAO;

    public DatumResources(DatumWrapperDAO datumWrapperDAO, Method52DAO method52DAO) {
        this.datumWrapperDAO = datumWrapperDAO;
        this.method52DAO = method52DAO;
    }

    @GET
    @Path("sql")
    public Response sql(@QueryParam("sql") String sql) throws SQLException {

        List<DatumWrapper> data = datumWrapperDAO.select(sql);

        return Response.status(Response.Status.OK).entity(
                data
        ).build();
    }

    @POST
    @Path("select-distinct")
    public Response selectDistinct(@QueryParam("table") String table,
                           @QueryParam("expression") String expression,
                           @QueryParam("distinctKey") String key,
                           Map<String, Map<String, String>> literalSpec
    ) throws SQLException {

        KeySet keys = method52DAO.schema(table);

        Key distinctKey = keys.get(key);

        Map<String, KeyFilter> literals = processLiterals(literalSpec, keys);

        LogicParser parser = new LogicParser(literals);

        DatumFilter datumFilter = parser.parse(null, expression);

        String sql = new SelectDistinct(table, distinctKey, datumFilter).sql();

            List<String> data = datumWrapperDAO.selectString(sql);

        return Response.status(Response.Status.OK).entity(
                data
        ).build();
    }

    @POST
    @Path("select")
    public Response select(@QueryParam("table") String table,
                          @QueryParam("expression") String expression,
                          Map<String, Map<String, String>> literalSpec
    ) throws SQLException {

        Map<String, KeyFilter> literals = processLiterals(literalSpec, method52DAO.schema(table));

        LogicParser parser = new LogicParser(literals);

        DatumFilter datumFilter = parser.parse(null, expression);

        String sql = new Select(table, datumFilter, ImmutableList.of(), 0).sql();

        List<DatumWrapper> data = datumWrapperDAO.select(sql);

        return Response.status(Response.Status.OK).entity(
                data
        ).build();
    }


    @POST
    @Path("proxy")
    public Response proxy(@QueryParam("table") String table,
                            @QueryParam("target") String target,
                            @QueryParam("proxy") String proxy,
                            @QueryParam("partition") String partition,
                            @QueryParam("orderBy") String orderByKey,
                            @QueryParam("proximity") int proximity,
                            @QueryParam("limit") int limit,
                            Map<String, Map<String, String>> literalSpec
    ) throws SQLException {

        KeySet keys = method52DAO.schema(table);

        Key partitionKey = keys.get(partition);

        Map<String, KeyFilter> literals = processLiterals(literalSpec, keys);

        LogicParser parser = new LogicParser(literals);

        DatumFilter targetFilter = parser.parse(null, target);
        DatumFilter proxyFilter = parser.parse(null, proxy);
        OrderBy orderBy = processOrderBy(orderByKey);

        String sql = new Proxy(table, targetFilter, proxyFilter, partitionKey, proximity, orderBy, limit).sql();

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
