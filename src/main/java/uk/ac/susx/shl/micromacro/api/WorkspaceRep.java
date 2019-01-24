package uk.ac.susx.shl.micromacro.api;

import java.io.Serializable;
import java.util.*;

public class WorkspaceRep implements Serializable {

    public String id;
    public String name;
    public Map<String, QueryRep> queries;

    public WorkspaceRep() {
        queries = new HashMap<>();
    }
}
