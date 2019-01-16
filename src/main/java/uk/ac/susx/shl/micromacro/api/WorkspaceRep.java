package uk.ac.susx.shl.micromacro.api;

import uk.ac.susx.shl.micromacro.core.Query;
import uk.ac.susx.shl.micromacro.core.Workspace;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorkspaceRep {

    public String id;
    public String name;

    public WorkspaceRep(){}

    public WorkspaceRep(Workspace workspace){
        id = workspace.id();
        name = workspace.name();
    }

}
