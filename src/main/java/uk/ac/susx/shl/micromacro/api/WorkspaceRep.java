package uk.ac.susx.shl.micromacro.api;

import uk.ac.susx.shl.micromacro.core.Query;
import uk.ac.susx.shl.micromacro.core.Workspace;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorkspaceRep {

    public String id;
    public String name;
    public Map<String, AbstractQueryRep> queries;
}
