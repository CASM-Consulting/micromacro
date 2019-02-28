package uk.ac.susx.shl.micromacro.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.apache.poi.ss.formula.functions.T;
import uk.ac.susx.shl.micromacro.api.*;
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
import java.util.*;
import java.util.stream.Collectors;

public class QueryFactory {

    private final Method52DAO method52DAO;

    private final Gson gson;

    private static final String TYPE = "_TYPE";
    private static final String PROXY = "proxy";
    private static final String SELECT = "select";
    private static final String SELECT_DISTINCT = "select_distinct";

    private final static String KEY_NAME = "key";
    private final static String ARGS = "args";
    private final static String FILTER_TYPE = "type";


    public QueryFactory(Method52DAO method52DAO, Gson gson) {
        this.method52DAO = method52DAO;
        this.gson = gson;
    }

//    public Partition partition(PartitionRep rep) {
//
//        Key argKey = rep.argKey.isPresent() ? null : Key.of(rep.argKey.get());
//
//        Key partitionKey = Key.of(rep.partitionKey);
//
//        OrderBy orderBy = rep.orderBy.isPresent() ? null : OrderBy.asc(Key.of(rep.orderBy.get()));
//
//        return new Partition(Partition.Function.valueOf(rep.function),argKey, partitionKey, orderBy);
//    }
//
//    public Select select(SelectRep rep) throws SQLException {
//        KeySet keys = method52DAO.schema(rep.table);
//
//        Map<String, KeyFilter> literals = processLiterals(rep.literals, keys);
//
//        LogicParser parser = new LogicParser(literals);
//
//        DatumFilter datumFilter = parser.parse(null, rep.filter);
//
//        Partition partition = null;
////        if(rep.partition.isPresent()) {
////
////            partition = partition(rep.partition.get());
////        }
//
//        Select select = new Select(rep.table, partition, datumFilter, ImmutableList.of(), rep.limit, rep.offset);
//
//        return select;
//    }
//
//    public SelectDistinct selectDistinct(SelectDistinctRep rep) throws SQLException {
//        KeySet keys = method52DAO.schema(rep.table);
//
//        Key distinctKey = keys.get(rep.distinctKey);
//
//        Map<String, KeyFilter> literals = processLiterals(rep.literals, keys);
//
//        LogicParser parser = new LogicParser(literals);
//
//        DatumFilter datumFilter = parser.parse(null, rep.filter);
//
//        SelectDistinct selectDistinct = new SelectDistinct(rep.table, distinctKey, datumFilter);
//
//        return selectDistinct;
//    }
//
//    public Proxy proxy(ProxyRep rep) throws SQLException {
//        KeySet keys = method52DAO.schema(rep.table);
//
//        Key partitionKey = keys.get(rep.partitionKey);
//
//        if(partitionKey == null) {
//            partitionKey = Key.of(rep.partitionKey, RuntimeType.ANY);
//        }
//
//        Map<String, KeyFilter> literals = processLiterals(rep.literals, keys);
//
//        LogicParser parser = new LogicParser(literals);
//
//        DatumFilter targetFilter = parser.parse(null, rep.target);
//        DatumFilter proxyFilter = parser.parse(null, rep.proxy);
//        OrderBy orderBy = processOrderBy(rep.orderBy);
//        orderBy = orderBy.numeric(true);
//
//        Proxy proxy = new Proxy(rep.table, targetFilter, proxyFilter, partitionKey, rep.proximity, orderBy,
//                rep.innerLimit, rep.innerOffset, rep.outerLimit);
//
//        return proxy;
//    }
//
//    public ProxyRep proxy(Proxy proxy) {
//        ProxyRep rep = new ProxyRep();
//        rep.type = "proxy";
//        rep.table = proxy.table();
//        rep.innerLimit = proxy.innerLimit();
//        rep.innerOffset = proxy.innerOffset();
//        rep.outerLimit = proxy.outerLimit();
//        rep.orderBy = proxy.orderBy().key().toString();
//        rep.proximity = proxy.proximity();
//        rep.partitionKey = proxy.partitionKey().toString();
//
//        Map<KeyFilter, String> slaretil = new HashMap<>();
//
//        rep.target = proxy.targetFilter().encode(slaretil);
//        rep.proxy  = proxy.proxyFilter().encode(slaretil);
//
//        Map<String, KeyFilter> literals = slaretil
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
//
//
//        rep.literals = processLiterals(literals);
//
//        return rep;
//    }
//
//
//    public SelectRep select(Select select) {
//        SelectRep rep = new SelectRep();
//        rep.type = "select";
//        rep.table = select.table();
//        rep.limit = select.limit();
//        rep.offset = select.offset();
////        rep.orderBy = select.orderBy().key().toString();
//        rep.orderBy = ImmutableList.of();
//        if(select.partition() != null) {
//            PartitionRep partition = new PartitionRep();
//            partition.function = select.partition().function().name();
//            partition.partitionKey = select.partition().partitionKey().toString();
//            if(select.partition().argKey() != null) {
//                partition.argKey = Optional.of(select.partition().argKey().toString());
//            }
//            if(select.partition().orderBy() != null) {
//                partition.orderBy = Optional.of(select.partition().orderBy().key().toString());
//            }
////            rep.partition = Optional.of(partition);
//        }
//
//        Map<KeyFilter, String> slaretil = new HashMap<>();
//
//        rep.filter = select.where().encode(slaretil);
//
//        Map<String, KeyFilter> literals = slaretil
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
//
//
//        rep.literals = processLiterals(literals);
//
//        return rep;
//    }
//
//    public SelectDistinctRep selectDistinct(SelectDistinct selectDistinct) {
//        SelectDistinctRep rep = new SelectDistinctRep();
//        rep.type = "selectDistinct";
//        rep.table = selectDistinct.table();
////        rep.orderBy = select.orderBy().key().toString();
//
//        Map<KeyFilter, String> slaretil = new HashMap<>();
//
//        rep.filter = selectDistinct.filter().encode(slaretil);
//        rep.distinctKey = selectDistinct.distinctKey().toString();
//
//        Map<String, KeyFilter> literals = slaretil
//                .entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
//
//
//        rep.literals = processLiterals(literals);
//
//        return rep;
//    }


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
            if(key == null ) {
                key = Key.of(spec.get("key"), RuntimeType.ANY);
            }
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

    public DatumQuery query(Map rep) {

        String type = (String)rep.get(TYPE);

        rep.remove(TYPE);

        String json = gson.toJson(rep);

        DatumQuery query;

        if(type.equals(PROXY)) {
            query = gson.fromJson(json, Proxy.class);
        } else if(type.equals(SELECT)) {
            query = gson.fromJson(json, Select.class);
        } else if(type.equals(SELECT_DISTINCT)) {
            query = gson.fromJson(json, SelectDistinct.class);
        } else {
            throw new UnrecognisedQueryException();
        }

        return query;
    }



    public <T extends DatumQuery> Map rep(T query) {

        Map rep = gson.fromJson(gson.toJson(query), Map.class);

        if(query instanceof Proxy) {
            rep.put(TYPE, PROXY);
        } else if(query instanceof Select) {
            rep.put(TYPE, SELECT);
        } else if(query instanceof SelectDistinct) {
            rep.put(TYPE, SELECT_DISTINCT);
        } else {
            throw new UnrecognisedQueryException();
        }

        return rep;

    }

    public List<String> keys(Collection<Key> keys) {
        return keys.stream().map(key->key.toString()).collect(Collectors.toList());
    }

}
