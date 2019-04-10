package uk.ac.susx.shl.micromacro.core;

import uk.ac.susx.tag.method51.core.meta.Key;

import java.util.List;

public class GeoMap {
    private final String id;
    private final List<String> queries;
    private final Key geoKey;
    private final Key idKey;

    public GeoMap(String id, List<String> queries, Key geoKey, Key idKey) {
        this.id = id;
        this.queries = queries;
        this.geoKey = geoKey;
        this.idKey = idKey;
    }

    public String id(){
        return id;
    }
    public List<String> queries(){
        return queries;
    }
    public Key geoKey(){
        return idKey;
    }
    public Key idKey(){
        return geoKey;
    }
}
