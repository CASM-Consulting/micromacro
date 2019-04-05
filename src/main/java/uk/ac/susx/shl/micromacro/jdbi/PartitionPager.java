package uk.ac.susx.shl.micromacro.jdbi;

import org.mapdb.DB;
import uk.ac.susx.tag.method51.core.data.store2.query.Partitioner;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlQuery;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PartitionPager implements BiFunction<SqlQuery, Object, Function<String, String>> {

    private final Function<String, String> getter;

    public PartitionPager(Function<String, String> getter) {
        this.getter = getter;
    }

    @Override
    public Function<String, String> apply(SqlQuery query, Object obj) {
        CachingDAO<String, SqlQuery> cache = (CachingDAO<String, SqlQuery>)obj;
        AtomicReference<String> partitionId = new AtomicReference<>("");
        AtomicInteger page = new AtomicInteger(0);
        AtomicInteger i = new AtomicInteger(0);
        AtomicReference<int[]> pageIndices = new AtomicReference<>(null);
        Map<Integer, int[]> pageCache = cache.pageCache(cache.getQueryId(query));

        return (String d) -> {

            String partition = getter.apply(d);

            if(!partition.equals(partitionId.get())) {
                if(pageIndices.get() == null) {
                    pageIndices.getAndSet(new int[]{0,0});
                } else {

                    pageCache.put(page.get(), pageIndices.get());
                    pageIndices.getAndSet(new int[]{i.get(),0});
                    page.incrementAndGet();
                }

//                int pageNo = cache.pages.size();
//                cache.partitions.put(partition, pageNo);
            }
            partitionId.set(partition);
            pageIndices.get()[1] = i.incrementAndGet();
            return d;
        };
    }
}
