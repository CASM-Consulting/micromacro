package uk.ac.susx.shl.micromacro.core;

import uk.ac.susx.tag.method51.core.meta.Key;

import java.util.List;
import java.util.Map;

public class GeoMap {
    private final String id;
    private final List<String> queries;
    private final Key geoKey;
    private final Key idKey;
    private final Map options;
    private final Map<String, Object> metadata;

    public GeoMap(String id, List<String> queries, Key geoKey, Key idKey, Map options, Map<String, Object> metadata) {
        this.id = id;
        this.queries = queries;
        this.geoKey = geoKey;
        this.idKey = idKey;
        this.options = options;
        this.metadata = metadata;
    }

    public String id(){
        return id;
    }
    public List<String> queries(){
        return queries;
    }
    public Key geoKey(){
        return geoKey;
    }
    public Key idKey(){
        return idKey;
    }
    public Map options() {
        return options;
    }
    public Map<String, Object> metadata() {
        return metadata;
    }
}
