package uk.ac.susx.shl.micromacro.core;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import uk.ac.susx.shl.micromacro.api.AbstractDatumQueryRep;
import uk.ac.susx.shl.micromacro.api.DatumRep;
import uk.ac.susx.shl.micromacro.api.QueryRep;
import uk.ac.susx.shl.micromacro.api.WorkspaceRep;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class QueryResultCache {

    private final Map<AbstractDatumQueryRep, List<DatumRep>> cache;

    private final DB db;

    public QueryResultCache(Path queryCachePath) {

        db = DBMaker
                .fileDB(queryCachePath.toFile())
                .fileMmapEnable()
                .closeOnJvmShutdown()
//                .readOnly()
                .make();

        cache = (Map<AbstractDatumQueryRep, List<DatumRep>>) db.hashMap("workspaces").createOrOpen();

    }


    public <T extends AbstractDatumQueryRep> List<DatumRep> cache(T query, Supplier<List<DatumRep>> dataSupplier) {
        if(cache.containsKey(query)) {
            return cache.get(query);
        } else {
            List<DatumRep> results = dataSupplier.get();
            cache.put(query, results);
            db.commit();
            return results;
        }
    }


}
