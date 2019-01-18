package uk.ac.susx.shl.micromacro.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.susx.shl.micromacro.api.AbstractQueryRep;
import uk.ac.susx.shl.micromacro.api.ProxyRep;
import uk.ac.susx.shl.micromacro.api.SelectDistinctRep;
import uk.ac.susx.shl.micromacro.api.SelectRep;
import uk.ac.susx.shl.micromacro.jdbi.Method52DAO;
import uk.ac.susx.tag.method51.core.data.store2.query.*;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.filters.DatumFilter;
import uk.ac.susx.tag.method51.core.meta.filters.KeyFilter;
import uk.ac.susx.tag.method51.core.meta.filters.KeyFilters;
import uk.ac.susx.tag.method51.core.meta.filters.impl.AbstractKeyFilter;
import uk.ac.susx.tag.method51.core.meta.filters.logic.LogicParser;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    public ProxyRep proxy(Proxy proxy) {
        ProxyRep rep = new ProxyRep();
        rep.type = "proxy";
        rep.table = proxy.table();
        rep.limit = proxy.limit();
        rep.orderBy = proxy.orderBy().key().toString();
        rep.proximity = proxy.proximity();
        rep.partitionKey = proxy.partitionKey().toString();

        Map<KeyFilter, String> slaretil = new HashMap<>();

        rep.target = proxy.targetFilter().encode(slaretil);
        rep.proxy  = proxy.proxyFilter().encode(slaretil);

        Map<String, KeyFilter> literals = slaretil
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));


        rep.literals = processLiterals(literals);

        return rep;
    }


    public SelectRep select(Select select) {
        SelectRep rep = new SelectRep();
        rep.type = "select";
        rep.table = select.table();
        rep.limit = select.limit();
//        rep.orderBy = select.orderBy().key().toString();
        rep.orderBy = ImmutableList.of();

        Map<KeyFilter, String> slaretil = new HashMap<>();

        rep.filter = select.filter().encode(slaretil);

        Map<String, KeyFilter> literals = slaretil
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));


        rep.literals = processLiterals(literals);

        return rep;
    }

    public SelectDistinctRep selectDistinct(SelectDistinct selectDistinct) {
        SelectDistinctRep rep = new SelectDistinctRep();
        rep.type = "selectDistinct";
        rep.table = selectDistinct.table();
//        rep.orderBy = select.orderBy().key().toString();

        Map<KeyFilter, String> slaretil = new HashMap<>();

        rep.filter = selectDistinct.filter().encode(slaretil);
        rep.distinctKey = selectDistinct.distinctKey().toString();

        Map<String, KeyFilter> literals = slaretil
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));


        rep.literals = processLiterals(literals);

        return rep;
    }


    private Map<String, Map<String, String>> processLiterals(Map<String, KeyFilter> literals) {
        Map<String, Map<String, String>> processed = new HashMap<>();

        for(Map.Entry<String, KeyFilter> entry : literals.entrySet()){
            AbstractKeyFilter filter = (AbstractKeyFilter)entry.getValue();
            Map<String, String> lit = ImmutableMap.of(
                "key", filter.key().toString(),
                "args", filter.args(),
                "type", filter.type()
            );
            processed.put(entry.getKey(), lit);
        }
        return processed;
    }

    private Map<String, KeyFilter> processLiterals(Map<String, Map<String, String>> specs, KeySet keys) {

        Map<String, KeyFilter> literals = new HashMap<>();

        for(Map.Entry<String, Map<String, String>> entry : specs.entrySet()) {
            Map<String, String> spec = entry.getValue();

            Key key = keys.get(spec.get("key"));
            String args = spec.get("args");
            String name = spec.get("type");

            KeyFilter filter = KeyFilters.get(name, args, key);

            literals.put(entry.getKey(), filter);
        }

        return literals;
    }

    private OrderBy processOrderBy(String key) {
        return OrderBy.asc(Key.of(key, RuntimeType.ANY));
    }

    public <T extends AbstractQueryRep> DatumQuery query(T rep) throws SQLException {
        if(rep instanceof ProxyRep) {
            return proxy((ProxyRep)rep);
        } else if(rep instanceof SelectRep) {
            return select((SelectRep)rep);
        } else if(rep instanceof SelectDistinctRep) {
            return selectDistinct((SelectDistinctRep)rep);
        } else {
            throw new UnrecognisedQueryException();
        }
    }

    public <T extends DatumQuery> AbstractQueryRep rep(T query) {
        if(query instanceof Proxy) {
            return proxy((Proxy)query);
        } else if(query instanceof Select) {
            return select((Select)query);
        } else if(query instanceof SelectDistinct) {
            return selectDistinct((SelectDistinct)query);
        } else {
            throw new UnrecognisedQueryException();
        }

    }

}
