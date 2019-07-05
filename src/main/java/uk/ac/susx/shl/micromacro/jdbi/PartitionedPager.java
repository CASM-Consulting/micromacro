package uk.ac.susx.shl.micromacro.jdbi;

import com.google.gson.Gson;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlQuery;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

public class PartitionedPager implements BiFunction<SqlQuery, Object, Function<String, String>> {

    private static final Logger LOG = Logger.getLogger(PartitionedPager.class.getName());

    private final Function<String, String> getter;

    public static final String ID2PAGE = "-id2Page";
    public static final String ID2INTARR = "-id2IntArr";

    public PartitionedPager(String key) {

        Gson gson = GsonBuilderFactory.get().create();

        this.getter = (d->gson.fromJson(d, Map.class).get(key).toString());
    }

    @Override
    public Function<String, String> apply(SqlQuery query, Object obj) {
        CachingDAO<String, SqlQuery> cache = (CachingDAO<String, SqlQuery>)obj;
        AtomicReference<String> partitionId = new AtomicReference<>("");
        AtomicInteger page = new AtomicInteger(0);
        AtomicInteger i = new AtomicInteger(0);
        AtomicReference<int[]> pageIndices = new AtomicReference<>(new int[]{0,0});

        String id = cache.getQueryId(query);
        Map<String, Integer> id2PageCache = cache.str2Int(id, ID2PAGE);
        Map<Integer, int[]> pageCache = cache.int2IntArr(id, ID2INTARR);

        return (String d) -> {

            try {

                String partition = getter.apply(d);

                if(!partitionId.get().equals(""))  {
                    if(!partition.equals(partitionId.get())) {
                        id2PageCache.put(partitionId.get(), page.get());

                        pageCache.put(page.get(), pageIndices.get());
                        pageIndices.getAndSet(new int[]{i.get(), 0});
                        page.incrementAndGet();
                    }
                }

                partitionId.set(partition);
                pageIndices.get()[1] = i.incrementAndGet();
            } catch (NullPointerException e) {
                LOG.warning("partition key not present :" + e.getMessage());
            }
            return d;
        };
    }
}
