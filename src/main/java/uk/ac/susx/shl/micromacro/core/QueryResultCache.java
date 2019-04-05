package uk.ac.susx.shl.micromacro.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.mapdb.*;
import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;
import uk.ac.susx.tag.method51.core.data.store2.query.Partitioner;


import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryResultCache {

    private static final Logger LOG = Logger.getLogger(QueryResultCache.class.getName());

    private final DB db;

    private final Map<String, String> queryIds;

    private final Cache<String, Lock> running;


    public QueryResultCache(Path queryCachePath) {

        running = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();

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

    private <T> Supplier<T> lockingSupplier(Lock lock, Supplier<T> supplier) {
        return () -> {
            try {
                lock.lock();
                System.out.println(lock.toString());
                T got = supplier.get();
                return got;
            } finally {
                lock.unlock();
                System.out.println(lock.toString());
            }
        };
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
                                                             BiFunction<T, CachedQueryResult<T>, Function<String, String>> pager)  {
        try {
            CachedQueryResult cached = new CachedQueryResult<>(query, resultSupplier, pager);
            return cached;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
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

        final Lock lock;

        public CachedQueryResult(T query, Supplier<Stream<String>> resultSupplier,
                                 BiFunction<T, CachedQueryResult<T>, Function<String, String>> pager) throws ExecutionException {
            id = getQueryId(query);
            result = resultCache(id);
            cached = cachedCache(id);
            pages = pageCache(id);
            partitions = partitionCache(id);

            lock = running.get(query.sql(), ReentrantLock::new);

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

        public Long count() {
            return lockingSupplier(lock, () -> {
                if(cached.get()) {
                    return (long)result.size();
                } else {
                    return resultStream.count();
                }
            }).get();
        }

        public Stream<String> stream() {
            return lockingSupplier(lock, () -> {
                if (cached.get()) {
                    return result.stream();
                } else {
                    return resultStream;
                }
            }).get();
        }

        public int[] indices(int page) {
            if(cached.get()) {
                return pages.get(page);
            } else {
                return new int[]{0,0};
            }
        }

        public List<String> get(int from, int to) {
            return lockingSupplier(lock, () -> {
                if(cached.get()) {
                    return result.subList(Math.min(result.size(),from), Math.min(result.size(),to));
                } else {
                    return resultStream.skip(from).limit(to-from).collect(Collectors.toList());
                }
            }).get();
        }

        public void clear() {
            pages.clear();
            result.clear();
            cached.set(false);
            db.commit();
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
