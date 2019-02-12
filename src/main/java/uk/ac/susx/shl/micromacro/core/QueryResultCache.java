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
import java.util.logging.Logger;

public class QueryResultCache {

    private static final Logger LOG = Logger.getLogger(QueryResultCache.class.getName());

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
        long tic = System.nanoTime();

        List<DatumRep> cached = cache.get(query);
        LOG.info("lookup time " + (System.nanoTime()-tic)/1000000);

        try {

            if(cached != null) {
                return cached;
            } else {
                LOG.info("query");
                List<DatumRep> results = dataSupplier.get();
                cache.put(query, results);
                db.commit();
                return results;
            }
        } finally {
            LOG.info("return time " + (System.nanoTime()-tic)/1000000);
        }
    }


}
