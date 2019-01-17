package uk.ac.susx.shl.micromacro.core;

import uk.ac.susx.shl.micromacro.api.AbstractQueryRep;
import uk.ac.susx.shl.micromacro.api.WorkspaceRep;
import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;

import java.sql.SQLException;
import java.util.*;

public class WorkspaceFactory {

    private final QueryFactory queryFactory;

    public WorkspaceFactory(QueryFactory queryFactory){

        this.queryFactory = queryFactory;
    }


    public Workspace workspace(WorkspaceRep rep) {

        Workspace workspace = new Workspace(rep.name, rep.id);

        for(Map.Entry<String, Queue<AbstractQueryRep>> entry : rep.queries.entrySet()) {

            try {
                DatumQuery currentQuery = queryFactory.query(entry.getValue().peek());
                workspace.add(entry.getKey(),currentQuery);
            } catch (SQLException e) {
            }
        }

        return workspace;
    }


    public WorkspaceRep rep(Workspace workspace) {

        WorkspaceRep rep = new WorkspaceRep();

        rep.id = workspace.id();
        rep.name = workspace.name();
        rep.queries = new HashMap<>();

        for(Map.Entry<String, Query> entry : workspace.queries().entrySet()) {

            Queue<AbstractQueryRep> queries = new LinkedList<>();

            for(Object query : entry.getValue().history()) {
                queries.add(queryFactory.rep((DatumQuery)query));
            }

            rep.queries.put(entry.getKey(), queries);

        }
        return rep;
    }


}
