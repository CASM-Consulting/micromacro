package uk.ac.susx.shl.micromacro.core;

import com.google.common.collect.ImmutableList;
import uk.ac.susx.shl.micromacro.api.ProxyRep;
import uk.ac.susx.shl.micromacro.api.SelectDistinctRep;
import uk.ac.susx.shl.micromacro.api.SelectRep;
import uk.ac.susx.shl.micromacro.jdbi.DatumWrapperDAO;
import uk.ac.susx.shl.micromacro.jdbi.Method52DAO;
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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class QueryFactory {

    private final Method52DAO method52DAO;


    private final static String KEY_NAME = "key";
    private final static String ARGS = "args";
    private final static String FILTER_NAME = "filter";


    public QueryFactory(Method52DAO method52DAO) {
        this.method52DAO = method52DAO;
    }

    public Select select(SelectRep rep) throws SQLException {
        KeySet keys = method52DAO.schema(rep.table);

        Map<String, KeyFilter> literals = processLiterals(rep.literals, keys);

        LogicParser parser = new LogicParser(literals);

        DatumFilter datumFilter = parser.parse(null, rep.filter);

        Select select = new Select(rep.table, datumFilter, ImmutableList.of(), rep.limit);

        return select;
    }

    public SelectDistinct selectDistinct(SelectDistinctRep rep) throws SQLException {
        KeySet keys = method52DAO.schema(rep.table);

        Key distinctKey = keys.get(rep.distinctKey);

        Map<String, KeyFilter> literals = processLiterals(rep.literals, keys);

        LogicParser parser = new LogicParser(literals);

        DatumFilter datumFilter = parser.parse(null, rep.filter);

        SelectDistinct selectDistinct = new SelectDistinct(rep.table, distinctKey, datumFilter);

        return selectDistinct;
    }

    public Proxy proxy(ProxyRep rep) throws SQLException {
        KeySet keys = method52DAO.schema(rep.table);

        Key partitionKey = keys.get(rep.partitionKey);

        Map<String, KeyFilter> literals = processLiterals(rep.literals, keys);

        LogicParser parser = new LogicParser(literals);

        DatumFilter targetFilter = parser.parse(null, rep.target);
        DatumFilter proxyFilter = parser.parse(null, rep.proxy);
        OrderBy orderBy = processOrderBy(rep.orderBy);
        orderBy = orderBy.numeric(true);

        Proxy proxy = new Proxy(rep.table, targetFilter, proxyFilter, partitionKey, rep.proximity, orderBy, rep.limit);

        return proxy;
    }

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
