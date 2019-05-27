package uk.ac.susx.shl.micromacro.core;

import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;
import uk.ac.susx.tag.method51.core.meta.filters.KeyFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Workspace {

    private final String id;
    private final String name;
    private final Map<String, Query> queries;
    private final Map<String, Map<String, KeyFilter>> tableLiterals;
    private final Map<String, GeoMap> maps;

    public Workspace(String name) {
        id = UUID.randomUUID().toString();
        this.name = name;
        queries = new HashMap<>();
        maps = new HashMap<>();
        tableLiterals = new HashMap<>();
    }

    public Workspace(String name, String id) {
        this.id = id;
        this.name = name;
        queries = new HashMap<>();
        maps = new HashMap<>();
        tableLiterals = new HashMap<>();
    }


    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public <T extends DatumQuery> Workspace addQuery(String name, T query) {
        if(!queries.containsKey(name)) {

            queries.put(name, new Query<T>());
            queries.get(name).add(query);

        } else if(!query.sql().equals(queries.get(name).get(0).sql())) {

            queries.get(name).add(query);
        }

        return this;
    }

    public Workspace setQuery(String name, Query query) {
        queries.put(name, query);
        return this;
    }

    public Workspace deleteQuery(String name) {
        queries.remove(name);
        return this;
    }

    public Map<String, Query> queries(){

        return queries;
    }

    public <T extends DatumQuery> Query<T> getQuery(String id) {
        return queries.get(id);
    }

    public GeoMap getMap(String id) {
        return maps.get(id);
    }

    public Workspace addMap(String id, GeoMap map) {
        maps.put(id, map);
        return this;
    }

    public Map<String,GeoMap> maps() {
        return maps;
    }

    public Workspace tableLiterals(String table, Map<String, KeyFilter> literals) {
        tableLiterals.put(table, literals);
        return this;
    }

    public Map<String, KeyFilter> tableLiterals(String table) {
        return tableLiterals.get(table);
    }

    public Map<String, Map<String, KeyFilter>> tableLiterals() {
        return tableLiterals;
    }
}
