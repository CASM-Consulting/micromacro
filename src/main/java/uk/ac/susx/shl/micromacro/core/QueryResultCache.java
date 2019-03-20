package uk.ac.susx.shl.micromacro.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import org.mapdb.*;
import uk.ac.susx.shl.micromacro.api.*;
import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;
import uk.ac.susx.tag.method51.core.data.store2.query.Partitioner;
import uk.ac.susx.tag.method51.core.data.store2.query.Proxy;
import uk.ac.susx.tag.method51.core.meta.Datum;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class QueryResultCache {

    private static final Logger LOG = Logger.getLogger(QueryResultCache.class.getName());

    private final DB db;

    private final Map<String, String> queryIds;

    public QueryResultCache(Path queryCachePath) {

        db = DBMaker
                .fileDB(queryCachePath.toFile())
                .concurrencyScale(4)
                .fileMmapEnable()
//                .fileChannelEnable()
                .closeOnJvmShutdown()
//                .readOnly()
                .make();

        queryIds = db.hashMap("queryIds", Serializer.STRING, Serializer.STRING).createOrOpen();
    }


    private Map<Integer, int[]> pageCache(String id) {
        Map<Integer, int[]> pages = db.hashMap(id +"-pages", Serializer.INTEGER, Serializer.INT_ARRAY).createOrOpen();
        return pages;
    }

    private Map<String, Integer> partitionCache(String id) {
        Map<String, Integer> partitions = db.hashMap(id +"-partitions", Serializer.STRING, Serializer.INTEGER).createOrOpen();
        return partitions;
    }

    private List<String> resultCache(String id) {
        List<String> result = db.indexTreeList(id, Serializer.STRING).createOrOpen();
        return result;
    }

    private Atomic.Boolean cachedCache(String id) {
        Atomic.Boolean cached = db.atomicBoolean(id +"-cached").createOrOpen();
        return cached;
    }

    public void clearCacheAll() {
        for(String id : queryIds.values()) {
            clearCache(id);
        }
    }

    public <T extends DatumQuery> void clearCache(T query) {
        String id = getQueryId(query);
        clearCache(id);
    }

    public void clearCache(String id) {
        Map<Integer, int[]> pages = pageCache(id);
        List<String> result = resultCache(id);
        Atomic.Boolean cached = cachedCache(id);
        pages.clear();
        result.clear();
        cached.set(false);
        db.commit();
    }

    public <T extends DatumQuery> CachedQueryResult<T> cache(T query, Supplier<Stream<String>> dataSupplier) {
        return cache(query, dataSupplier, (q, c) -> d -> d);
    }

    public <T extends DatumQuery> CachedQueryResult<T> cache(T query, Supplier<Stream<String>> resultSupplier,
                                                             BiFunction<T, CachedQueryResult<T>, Function<String, String>> pager) {

        CachedQueryResult cached = new CachedQueryResult<>(query, resultSupplier, pager);

        return cached;
    }

    public <T extends DatumQuery> boolean isCached(T query) {
        String id = getQueryId(query);
        boolean cached = db.atomicBoolean(id +"-cached").createOrOpen().get();
        return cached;
    }

    public <T extends DatumQuery> String getQueryId(T query) {
        String id;
        String sql = query.sql();

        if(queryIds.containsKey(sql)) {
            id = queryIds.get(sql);
        } else {
            id = UUID.randomUUID().toString();
            queryIds.put(sql, id);
            db.commit();
        }
        return id;
    }

    public class CachedQueryResult<T extends DatumQuery> implements AutoCloseable {

        public final String id;
        public final Map<Integer, int[]> pages;
        public final Map<String, Integer> partitions;
        public final List<String> result;
        public final Stream<String> resultStream;
        private final Atomic.Boolean cached;


        public CachedQueryResult(T query, Supplier<Stream<String>> resultSupplier,
                                 BiFunction<T, CachedQueryResult<T>, Function<String, String>> pager) {
            id = getQueryId(query);
            result = resultCache(id);
            cached = cachedCache(id);
            pages = pageCache(id);
            partitions = partitionCache(id);

            if(cached.get()) {
                resultStream = result.stream();
            } else {
                resultStream = resultSupplier.get().sequential()
                        .map(datumRep -> {
                            result.add(datumRep);
                            return datumRep;
                        })
                        .map(pager.apply(query, this))
                        .onClose(()-> {
                            cached.set(true);
                            db.commit();
                        });
            }
        }

        public long count() {
            if(cached.get()) {
                return result.size();
            } else {
                return resultStream.count();
            }
        }

        public Stream<String> stream() {
            return resultStream;
        }

        public int[] indices(int page) {
            return pages.get(page);
        }

        public List<String> get(int from, int to) {
            return result.subList(Math.min(result.size(),from), Math.min(result.size(),to));
        }

        @Override
        public void close() {
            db.commit();
            resultStream.close();
        }
    }

    public static class PartitionPager<T extends DatumQuery & Partitioner> implements BiFunction<T, CachedQueryResult<T>, Function<String, String>> {

        private final Function<String, Map> mapper;
        public PartitionPager(Function<String, Map> mapper) {
            this.mapper = mapper;
        }

        @Override
        public Function<String, String> apply(T query, CachedQueryResult<T> cache) {
            AtomicReference<String> partitionId = new AtomicReference<>("");
            AtomicInteger page = new AtomicInteger(0);
            AtomicInteger i = new AtomicInteger(0);
            AtomicReference<int[]> pageIndices = new AtomicReference<>(null);
            return (String d) -> {

                Map datum = mapper.apply(d);

                String partition = datum.get(query.partition().key().toString()).toString();

                if(!partition.equals(partitionId.get())) {
                    if(pageIndices.get() == null) {
                        pageIndices.getAndSet(new int[]{0,0});
                    } else {

                        cache.pages.put(page.get(), pageIndices.get());
                        pageIndices.getAndSet(new int[]{i.get(),0});
                        page.incrementAndGet();
                    }

                    int pageNo = cache.pages.size();
                    cache.partitions.put(partition, pageNo);
                }
                partitionId.set(partition);
                pageIndices.get()[1] = i.incrementAndGet();
                return d;
            };
        }
    }

}
