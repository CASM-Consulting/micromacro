package uk.ac.susx.shl.micromacro.core;

import uk.ac.susx.shl.micromacro.api.AbstractQueryRep;
import uk.ac.susx.shl.micromacro.api.WorkspaceRep;

import java.util.HashMap;
import java.util.Map;

public class WorkspaceFactory {

    private final QueryFactory queryFactory;
    private final Workspaces workspaces;

    public WorkspaceFactory(Workspaces workspaces, QueryFactory queryFactory){

        this.queryFactory = queryFactory;
        this.workspaces = workspaces;
    }


    public WorkspaceRep rep(String name) {

        Workspace workspace = workspaces.get(name);

        return rep(workspace);
    }


    public WorkspaceRep rep(Workspace workspace) {

        WorkspaceRep rep = new WorkspaceRep();

        rep.id = workspace.id();
        rep.name = workspace.name();
        rep.queries = new HashMap<>();

        for(Map.Entry<String, Query> entry : workspace.queries().entrySet()) {

            rep.queries.put(entry.getKey(), queryFactory.rep(entry.getValue().get()));

        }
        return rep;
    }


}
