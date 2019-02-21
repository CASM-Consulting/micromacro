package uk.ac.susx.shl.micromacro.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.IndexTreeList;
import org.mapdb.Serializer;
import uk.ac.susx.shl.micromacro.api.AbstractDatumQueryRep;
import uk.ac.susx.shl.micromacro.api.DatumRep;
import uk.ac.susx.shl.micromacro.api.QueryRep;
import uk.ac.susx.shl.micromacro.api.WorkspaceRep;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class QueryResultCache {

    private static final Logger LOG = Logger.getLogger(QueryResultCache.class.getName());

    private final DB db;


    public QueryResultCache(Path queryCachePath) {

        db = DBMaker
                .fileDB(queryCachePath.toFile())
                .concurrencyScale(4)
                .fileMmapEnable()
//                .fileChannelEnable()
                .closeOnJvmShutdown()
//                .readOnly()
                .make();
    }

    public <T extends AbstractDatumQueryRep> List<Object> get(T query) {
        try {
            String json = new ObjectMapper().writeValueAsString(query);
            String hash = DigestUtils.sha1Hex(json);
            List<Object> data = db.indexTreeList(hash).createOrOpen();
            return data;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends AbstractDatumQueryRep> Stream<DatumRep> cache(T query, Supplier<Stream<DatumRep>> dataSupplier) {

        long tic = System.nanoTime();

        List<Object> cached = get(query);
        LOG.info("lookup time " + (System.nanoTime()-tic)/1000000);

        try {

            if(!cached.isEmpty()) {
                return cached.stream().map(o -> (DatumRep)o);
            } else {
                LOG.info("query");
                Stream<DatumRep> dataStream = dataSupplier.get().map(datumRep -> {
                    cached.add(datumRep);
                    return datumRep;
                });
                db.commit();
                return dataStream;
            }
        } finally {
            LOG.info("return time " + (System.nanoTime()-tic)/1000000);
        }
    }


}
