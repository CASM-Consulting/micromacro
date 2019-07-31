package uk.ac.susx.shl.micromacro.jdbi;

import com.google.gson.Gson;
import uk.ac.susx.tag.method51.core.data.store2.query.Proximity;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlQuery;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

public class ChunkCounter implements StreamFunction<SqlQuery, String> {

    private static final Logger LOG = Logger.getLogger(ChunkCounter.class.getName());

    private final Function<Map, String> getter;

    public static final String ID2COUNT = "-id2Count";
    public static final String ROW_NUMER = "__row_number";
    public static final String TARGET = "__target";
    public static final String PROXIMITY = "__proximity";
    private final Gson gson = GsonBuilderFactory.get().create();

    public ChunkCounter(String key) {

        this.getter = (data->data.get(key).toString());

    }

    @Override
    public Function<String, String> apply(SqlQuery query, Object obj) {
        CachingDAO<String, SqlQuery> cache = (CachingDAO<String, SqlQuery>)obj;

        AtomicReference<String> prevParition = new AtomicReference<>("");
        AtomicInteger i = new AtomicInteger(0);
        AtomicInteger j = new AtomicInteger(0);
        AtomicLong prevRowNumber = new AtomicLong(-1);

//        int d = -1;
//        if(query instanceof Proximity) {
//            d = ((Proximity)query).distance();
//        }

        String id = cache.getQueryId(query);
        Map<String, Integer> id2CountCache = cache.str2Int(id, ID2COUNT);

        return (String d) -> {

            Map data = gson.fromJson(d, Map.class);
            try {

                long rowNumber = ((Double)data.get(ROW_NUMER)).longValue();
                boolean target = (Boolean)data.get(TARGET);
                boolean proximity = (Boolean)data.get(PROXIMITY);

                String partition = getter.apply(data);

                boolean partitionEnd = !prevParition.get().equals("") && !prevParition.get().equals(partition);
                boolean chunkEnd = prevRowNumber.get() != -1 && prevRowNumber.get() != rowNumber-1;

                if(partitionEnd || chunkEnd) {
                    i.addAndGet(j.get()-1);
                    j.set(0);
                }

                if(partitionEnd) {
                    id2CountCache.put(prevParition.get(), i.get());
                    prevRowNumber.set(-1);
                    i.set(0);
                    j.set(0);
                } else {
                    prevRowNumber.set(rowNumber);
                }
                prevParition.set(partition);

                if(proximity) {
                    j.incrementAndGet();
                }
                if(target) {
                    j.incrementAndGet();
                }

            } catch (NullPointerException e) {

                LOG.warning("partition key not present :" + e.getMessage());
            }
            return d;
        };
    }

    @Override
    public void end() {

    }
}
