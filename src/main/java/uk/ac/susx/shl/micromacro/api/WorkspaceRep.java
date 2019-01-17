package uk.ac.susx.shl.micromacro.api;

import uk.ac.susx.shl.micromacro.core.Query;
import uk.ac.susx.shl.micromacro.core.Workspace;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class WorkspaceRep implements Serializable {

    public String id;
    public String name;
    public Map<String, Queue<AbstractQueryRep>> queries;
}
