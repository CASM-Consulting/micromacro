package uk.ac.susx.shl.micromacro.jdbi;

import com.google.gson.Gson;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlQuery;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;

import java.util.HashMap;
import java.util.Map;
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
    private final Gson gson = GsonBuilderFactory.get().create();

    public ChunkCounter(String key) {

        this.getter = (data->data.get(key).toString());

    }

    @Override
    public Function<String, String> apply(SqlQuery query, Object obj) {
        CachingDAO<String, SqlQuery> cache = (CachingDAO<String, SqlQuery>)obj;
        AtomicReference<String> partitionId = new AtomicReference<>("");
        AtomicInteger i = new AtomicInteger(1);
        AtomicLong prevRowNumber = new AtomicLong(-1);

        String id = cache.getQueryId(query);
        Map<String, Integer> id2CountCache = cache.str2Int(id, ID2COUNT);

        return (String d) -> {

            Map data = gson.fromJson(d, Map.class);
            try {

                long rowNumber = ((Double)data.get(ROW_NUMER)).longValue();

                String partition = getter.apply(data);

                if(!partitionId.get().equals("")) {

                    if(!partition.equals(partitionId.get())) {

                        id2CountCache.put(partition, i.get());
                        LOG.info(partition + " " + i.get());
                        prevRowNumber.set(-1);
                        i.set(1);
                    } else if(prevRowNumber.get() != -1 && rowNumber != prevRowNumber.get()+1){
                        i.incrementAndGet();
                    }
                }
                prevRowNumber.set(rowNumber);
                partitionId.set(partition);

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
