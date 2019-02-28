package uk.ac.susx.shl.micromacro.core;

import uk.ac.susx.shl.micromacro.api.AbstractDatumQueryRep;
import uk.ac.susx.shl.micromacro.api.QueryRep;
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

        for(Map.Entry<String, QueryRep> entry : rep.queries.entrySet()) {
            QueryRep qrep = entry.getValue();

            LinkedList<? extends DatumQuery> history = qrep .history
                    .stream()
                    .map(queryFactory::query)
                    .collect(Collectors.toCollection(LinkedList::new));

            workspace.setQuery(entry.getKey(), new Query<>(history, qrep.metadata));
        }

        return workspace;
    }


    public <T extends DatumQuery> WorkspaceRep rep(Workspace workspace) {

        WorkspaceRep rep = new WorkspaceRep();

        rep.id = workspace.id();
        rep.name = workspace.name();
        rep.queries = new HashMap<>();

        for(Map.Entry<String, Query> entry : workspace.queries().entrySet()) {

            Query query = entry.getValue();

            QueryRep queryRep = new QueryRep();

            for(Object version : query.history()) {
                queryRep.history.add(queryFactory.rep((DatumQuery)version));
            }

            queryRep.metadata = query.getMeta();

            rep.queries.put(entry.getKey(), queryRep);

        }
        return rep;
    }


}
