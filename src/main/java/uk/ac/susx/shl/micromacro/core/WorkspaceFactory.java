package uk.ac.susx.shl.micromacro.core;

import uk.ac.susx.shl.micromacro.api.AbstractQueryRep;
import uk.ac.susx.shl.micromacro.api.WorkspaceRep;
import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WorkspaceFactory {

    private static final Logger LOG = Logger.getLogger(WorkspaceFactory.class.getName());

    private final QueryFactory queryFactory;

    public WorkspaceFactory(QueryFactory queryFactory){

        this.queryFactory = queryFactory;
    }


    public Workspace workspace(WorkspaceRep rep) {

        Workspace workspace = new Workspace(rep.name, rep.id);

        for(Map.Entry<String, List<AbstractQueryRep>> entry : rep.queries.entrySet()) {

            LinkedList<? extends DatumQuery> history = entry.getValue()
                    .stream()
                    .map(queryRep -> {
                        try {
                            return queryFactory.query(queryRep);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toCollection(LinkedList::new));

            workspace.setQuery(entry.getKey(), new Query<>(history));
        }

        return workspace;
    }


    public WorkspaceRep rep(Workspace workspace) {

        WorkspaceRep rep = new WorkspaceRep();

        rep.id = workspace.id();
        rep.name = workspace.name();
        rep.queries = new HashMap<>();

        for(Map.Entry<String, Query> entry : workspace.queries().entrySet()) {

            LinkedList<AbstractQueryRep> queries = new LinkedList<>();

            for(Object query : entry.getValue().history()) {
                queries.add(queryFactory.rep((DatumQuery)query));
            }

            rep.queries.put(entry.getKey(), queries);

        }
        return rep;
    }


}
