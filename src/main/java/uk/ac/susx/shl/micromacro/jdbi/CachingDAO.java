package uk.ac.susx.shl.micromacro.jdbi;

import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlQuery;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CachingDAO<T, Q extends SqlQuery> implements DAO<T,Q> {


    private final DB cache;
    private final DAO<T,Q> dao;

    private final Map<String, String> queryIds;

    public CachingDAO(DAO<T, Q> dao, Path queryCachePath) {
        this.dao = dao;
        cache = DBMaker
                .fileDB(queryCachePath.toFile())
                .concurrencyScale(4)
                .fileMmapEnable()
//                .fileChannelEnable()
                .closeOnJvmShutdown()
//                .readOnly()
                .make();
        queryIds = cache.hashMap("queryIds", Serializer.STRING, Serializer.STRING).createOrOpen();

    }

    @Override
    public DAO<T,Q> getDAO() {
        return dao;
    }

    public String getQueryId(Q query) {
        String id;
        String sql = query.sql();

        if(queryIds.containsKey(sql)) {
            id = queryIds.get(sql);
        } else {
            id = UUID.randomUUID().toString();
            queryIds.put(sql, id);
            cache.commit();
        }
        return id;
    }


    private Atomic.Boolean cachedCache(String id) {
        Atomic.Boolean cached = cache.atomicBoolean(id +"-cached").createOrOpen();
        return cached;
    }
    private List<String> resultCache(String id) {
        List<String> result = cache.indexTreeList(id, Serializer.STRING).createOrOpen();
        return result;
    }
    public Map<Integer, int[]> pageCache(String id) {
        Map<Integer, int[]> pages = cache.hashMap(id +"-pages", Serializer.INTEGER, Serializer.INT_ARRAY).createOrOpen();
        return pages;
    }

    public void clearCache(Q query) {
        String id = getQueryId(query);
        Map<Integer, int[]> pages = pageCache(id);
        List<String> result = resultCache(id);
        Atomic.Boolean cached = cachedCache(id);
        pages.clear();
        result.clear();
        cached.set(false);
        cache.commit();
    }

    private <R> R cache(Q query, Supplier<R> dao, Supplier<R> cache) {
        String id = getQueryId(query);
        Atomic.Boolean cached = cachedCache(id);
        if(cached.get()) {
            return cache.get();
        } else {
            return dao.get();
        }
    }


    @Override
    public Stream<T> stream(Q query) {
        return stream(query, (q, cache)->(s)->s);
    }

    @Override
    public Stream<T> stream(Q query, BiFunction<Q, Object, Function<T, T>>... functions) {
        Supplier<Stream<T>> daoSupplier = () -> {

            String id = getQueryId(query);
            final List<String> resultCache = resultCache(id);

            Stream<T> stream = dao.stream(query).map(result -> {
                resultCache.add(result.toString());
                return result;
            });

            for(BiFunction<Q, Object, Function<T, T>> function : functions) {
                stream = stream.map(function.apply(query,this));
            }

            stream = stream.onClose(()-> {
                cachedCache(id).set(true);
                cache.commit();
            });

            return stream;
        };

        Supplier<Stream<T>> cacheSupplier = () -> {
            String id = getQueryId(query);
            List resultCache = resultCache(id);
            return resultCache.stream();
        };

        return cache(query, daoSupplier, cacheSupplier);
    }


    @Override
    public List<T> list(Q query) {

        Supplier<List<T>> daoSupplier = () -> {
            throw new RuntimeException("query should be cached before accessed as list");
        };

        Supplier<List<T>> cacheSupplier = () -> {
            String id = getQueryId(query);
            final List resultCache = resultCache(id);
            return resultCache;
        };

        return cache(query, daoSupplier, cacheSupplier);
    }

    @Override
    public int update(Q query) {
        return dao.update(query);
    }
}
