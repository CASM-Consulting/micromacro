package uk.ac.susx.shl.micromacro.api;

import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class QueryRep implements Serializable {

    public List<Map> history;

    public Map<String, Object> metadata;

    public QueryRep() {
        history = new LinkedList<>();
        metadata = new HashMap<>();
    }

}
