package uk.ac.susx.shl.micromacro.jdbi;

import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlQuery;

import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CachingDAO<T, Q extends SqlQuery> implements DAO<T,Q> {

    public static final String RESULT = "-results";

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

    public List<String> strListCache(String id, String suffix) {
        register(id, suffix);
        List<String> result = cache.indexTreeList(id + suffix, Serializer.STRING).createOrOpen();
        return result;
    }

    public Map<String, Integer> str2Int(String id, String suffix) {
        register(id, suffix);
        Map<String, Integer> pages = cache.hashMap(id + suffix, Serializer.STRING, Serializer.INTEGER).createOrOpen();
        return pages;
    }

    public Map<Integer, int[]> int2IntArr(String id, String suffix) {
        register(id, suffix);
        Map<Integer, int[]> pages = cache.hashMap(id + suffix, Serializer.INTEGER, Serializer.INT_ARRAY).createOrOpen();
        return pages;
    }

    private Map<String, Set<String>> registry(String id) {
        Map<String, Set<String>> reg = cache.hashMap(id+"-reg", Serializer.STRING, Serializer.ELSA).createOrOpen();
        return reg;
    }
    private Set<String> caches(String id) {
        Map<String, Set<String>> reg = registry(id);
        if(!reg.containsKey(id)) {
            reg.put(id, new HashSet<>());
            cache.commit();
        }
        return reg.get(id);
    }

    private void register(String id, String suffix) {
        Map<String, Set<String>> reg = registry(id);
        if(!reg.containsKey(id)) {
            reg.put(id, new HashSet<>());
            cache.commit();
        }
        Set<String> caches = reg.get(id);
        caches.add(id+suffix);
        reg.put(id, caches);
    }

    public void clearCache(Q query) {
        String id = getQueryId(query);
        Set<String> caches = caches(id);

        for(String cacheName : caches) {
            Object c = cache.get(cacheName);
            if(Map.class.isAssignableFrom(c.getClass())) {
                ((Map)c).clear();
            } else if(List.class.isAssignableFrom(c.getClass())) {
                ((List)c).clear();
            }
        }
        Atomic.Boolean cached = cachedCache(id);
        cached.set(false);

//        Map<Integer, int[]> pages = pageCache(id);
//        List<String> result = strListCache(id);
//        pages.clear();
//        result.clear();
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
            final List<String> resultCache = strListCache(id, RESULT);

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
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {}
            });

            return stream;
        };

        Supplier<Stream<T>> cacheSupplier = () -> {
            String id = getQueryId(query);
            List resultCache = strListCache(id, RESULT);
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
            final List resultCache = strListCache(id, RESULT);
            return resultCache;
        };

        return cache(query, daoSupplier, cacheSupplier);
    }

    @Override
    public int update(Q query) {
        return dao.update(query);
    }
}
