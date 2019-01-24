package uk.ac.susx.shl.micromacro.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class QueryRep implements Serializable {

    public List<AbstractDatumQueryRep> history;

    public Map<String, Object> metadata;

    public QueryRep() {
        history = new LinkedList<>();
        metadata = new HashMap<>();
    }

}
