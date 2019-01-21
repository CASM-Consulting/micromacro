package uk.ac.susx.shl.micromacro.core;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import uk.ac.susx.shl.micromacro.api.WorkspaceRep;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Workspaces {
    private static final Logger LOG = Logger.getLogger(Workspaces.class.getName());

    private final Map<String, WorkspaceRep> workspaces;
    private final WorkspaceFactory workspaceFactory;
    private final DB db;

    public Workspaces(String workspaceMapPath, WorkspaceFactory workspaceFactory) {
        this.workspaceFactory = workspaceFactory;
        db = DBMaker
                .fileDB(workspaceMapPath)
                .fileMmapEnable()
                .closeOnJvmShutdown()
//                .readOnly()
                .make();

        workspaces = (Map<String, WorkspaceRep>) db.hashMap("workspaces").createOrOpen();
    }


    public Workspace get(String name) {
        return workspaceFactory.workspace(workspaces.get(name));
    }

    public Workspace create(String name) {
        Workspace workspace = new Workspace(name);

        workspaces.put(name, workspaceFactory.rep(workspace));

        return workspace;
    }

    public List<String> list() {
        return new ArrayList<>(workspaces.keySet());
    }

    public Workspaces save(Workspace workspace) {
        workspaces.put(workspace.name(), workspaceFactory.rep(workspace));
        db.commit();
        return this;
    }

}
