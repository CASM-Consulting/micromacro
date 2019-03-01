package uk.ac.susx.shl.micromacro.core;

import com.google.gson.Gson;
import uk.ac.susx.tag.method51.core.data.store2.query.*;
import uk.ac.susx.tag.method51.core.meta.Key;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class QueryFactory {

    private final Gson gson;

    private static final String TYPE = "_TYPE";
    private static final String PROXY = "proxy";
    private static final String SELECT = "select";
    private static final String SELECT_DISTINCT = "select_distinct";



    public QueryFactory(Gson gson) {
        this.gson = gson;
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
