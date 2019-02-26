package uk.ac.susx.shl.micromacro.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.mapdb.*;
import uk.ac.susx.shl.micromacro.api.*;


import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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


    public <T extends AbstractDatumQueryRep> CachedQueryResult<T> cache(T query, Supplier<Stream<DatumRep>> dataSupplier) {
        return cache(query, dataSupplier, (q, c) -> d -> d);
    }

    public <T extends AbstractDatumQueryRep> CachedQueryResult<T> cache(T query, Supplier<Stream<DatumRep>> resultSupplier,
                                                                    BiFunction<T, CachedQueryResult<T>, Function<DatumRep, DatumRep>> pager) {

        CachedQueryResult cached = new CachedQueryResult<>(query, resultSupplier, pager);

        return cached;
    }

    public class CachedQueryResult<T extends AbstractDatumQueryRep> implements AutoCloseable {

        public final String name;
        public final Map<Integer, int[]> pages;
        public final List<Object> result;
        public final Stream<DatumRep> resultStream;
        private final Atomic.Boolean cached;


        public CachedQueryResult(T rep, Supplier<Stream<DatumRep>> resultSupplier,
                                 BiFunction<T, CachedQueryResult<T>, Function<DatumRep, DatumRep>> pager) {
            try {
                String json = new ObjectMapper().writeValueAsString(rep);
                name = DigestUtils.sha1Hex(json);
                result = db.indexTreeList(name).createOrOpen();
                cached = db.atomicBoolean(name+"-cached").createOrOpen();
                pages = (Map<Integer,int[]>)db.hashMap(name+"-pages").createOrOpen();

                if(cached.get()) {
                    resultStream = (Stream)result.stream();
                } else {
                    resultStream = resultSupplier.get().sequential().map(datumRep -> {
                        result.add(datumRep);
                        return datumRep;
                    }).map(pager.apply(rep, this)).onClose(()-> {
                        cached.set(true);
                        db.commit();
                    });
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        public long count() {
            if(cached.get()) {
                return result.size();
            } else {
                return resultStream.count();
            }
        }

        public Stream<DatumRep> stream() {
            return resultStream;
        }

        public int[] indices(int page) {
            return pages.get(page);
        }

        public List<DatumRep> get(int from, int to) {
            return (List)result.subList(Math.min(result.size(),from), Math.min(result.size(),to));
        }

        @Override
        public void close() {
            db.commit();
            resultStream.close();
        }
    }

    public static class ProxyPager implements BiFunction<ProxyRep, CachedQueryResult<ProxyRep>, Function<DatumRep, DatumRep>> {

        @Override
        public Function<DatumRep, DatumRep> apply(ProxyRep proxyRep, CachedQueryResult cachedQueryResult) {
            AtomicReference<String> partitionId = new AtomicReference<>("");
            AtomicInteger page = new AtomicInteger(0);
            AtomicInteger i = new AtomicInteger(0);
            AtomicReference<int[]> pageIndices = new AtomicReference<>(null);
            return (DatumRep d) -> {
                String partition = d.data.get(proxyRep.partitionKey).toString();
                if(!partition.equals(partitionId.get())) {
                    if(pageIndices.get() == null) {
                        pageIndices.getAndSet(new int[]{0,0});
                    } else {
                        pageIndices.get()[1] = i.get();
                        cachedQueryResult.pages.put(page.get(), pageIndices.get());
                        pageIndices.getAndSet(new int[]{i.get(),0});
                        page.incrementAndGet();
                    }
                }
                partitionId.set(partition);
                i.incrementAndGet();
                return d;
            };
        }
    }

}
