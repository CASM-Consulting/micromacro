package uk.ac.susx.shl.micromacro.api;

import java.io.Serializable;
import java.util.*;

public class WorkspaceRep implements Serializable {

    public String id;
    public String name;
    public Map<String, QueryRep> queries;
    public Map<String, GeoMapRep> maps;
    public Map<String, Map> tableLiterals;

    public WorkspaceRep() {
        queries = new HashMap<>();
        maps = new HashMap<>();
        tableLiterals = new HashMap<>();
    }
}
