package uk.ac.susx.shl.micromacro.core;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Workspaces {

    private final Map<String, Workspace> workspaces;


    public Workspaces(String workspaceMapPath) {
        DB workspaceDb = DBMaker
                .fileDB(workspaceMapPath)
                .fileMmapEnable()
                .closeOnJvmShutdown()
//                .readOnly()
                .make();

        workspaces = (Map<String, Workspace>) workspaceDb.hashMap("workspaces").createOrOpen();
    }


    public Workspace get(String name) {
        return workspaces.get(name);
    }

    public Workspace create(String name) {
        Workspace workspace = new Workspace(name);

        workspaces.put(name, workspace);

        return workspace;
    }

    public Map<String, Workspace> get() {
        return workspaces;
    }

}
