package uk.ac.susx.shl.micromacro.resources;


import com.google.common.collect.ImmutableMap;
import uk.ac.susx.shl.micromacro.jdbi.CachingDAO;
import uk.ac.susx.shl.micromacro.jdbi.DAO;
import uk.ac.susx.shl.micromacro.jdbi.Method52DAO;
import uk.ac.susx.shl.micromacro.jdbi.PartitionedPager;
import uk.ac.susx.tag.method51.core.data.store2.query.*;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;


public class QueryResource<Q extends SqlQuery, U extends SqlUpdate> extends DAOStreamResource<String, Q> {

    private static final Logger LOG = Logger.getLogger(QueryResource.class.getName());

    private final Method52DAO method52DAO;

    public QueryResource(DAO<String, Q> datumDAO, Method52DAO method52DAO) {
        super(datumDAO);
        this.method52DAO = method52DAO;
    }

    /**
     *
     * @param asyncResponse
     * @param query
     * @throws Exception
     */
    public void query(AsyncResponse asyncResponse, Q query) throws Exception {
        daoStreamResponse(asyncResponse, query, (stream-> StreamSupport.stream(stream.spliterator(), false) ));
    }

    /**
     * Run the query for caching purposes only, don't return anything.
     * @param asyncResponse
     * @param query
     * @throws Exception
     */
    public void cacheOnly(AsyncResponse asyncResponse, Q query) throws Exception {
        daoStreamResponse(asyncResponse, query, (stream) -> {
            long total;
            if(isCached(query)) {
                if( isScopedOrPartitioned(query) ) {
                    total = getPages(query).size();
                } else {
                    total = datumDAO.list(query).size();
                }
            } else {
                total = stream.count();
            }

            return total;
        });
    }

    /**
     * Return [offset : offset + limit] of query results.
     * @param asyncResponse
     * @param offset
     * @param limit
     * @param query
     * @throws Exception
     */
    public void skipLimit(AsyncResponse asyncResponse, int offset, int limit, Q query) throws Exception {

        daoStreamResponse(asyncResponse, query, (stream-> {
            List<String> list = datumDAO.list(query);
            return list.subList(Math.min(list.size(), offset), Math.min(list.size(), offset + limit)).stream();
        }));
    }


    /**
     * Index directly to a page of the query response. Cache if not already cached (slow).
     * @param asyncResponse
     * @param page
     * @param query
     * @throws Exception
     */
    public void page(AsyncResponse asyncResponse, int page, Q query) throws Exception {
        daoStreamResponse(asyncResponse, query, (stream-> {
            CachingDAO<String, SqlQuery> cache = (CachingDAO<String, SqlQuery>)datumDAO.getDAO();

            if(!isCached(query)) {
                stream.count();
            }

            int[] indices = cache.int2IntArr(cache.getQueryId(query), PartitionedPager.ID2INTARR).get(page);

            if(indices == null) {
                return new ArrayList<>();
            } else {
                List<String> list = datumDAO.list(query);
                return list.subList(Math.min(list.size(), indices[0]), Math.min(list.size(), indices[1])).stream();
            }
        }));

    }


    /**
     * Index to a partition of a query response.
     * @param asyncResponse
     * @param partition
     * @param query
     * @throws Exception
     */
    public void partition(AsyncResponse asyncResponse, String partition, Q query) throws Exception {

        daoStreamResponse(asyncResponse, query, (stream-> {
            return partition(query, partition).stream();
        }));
    }

    private List<String> partition(Q query, String partition) {
        List<String> result = new ArrayList<>();
        if(isPartitioned(query)) {

            CachingDAO<String, Q> cache = getCache();

            String id = cache.getQueryId(query);

            Map<String, Integer> partitions = cache.str2Int(id, PartitionedPager.ID2PAGE);

            if(partitions.containsKey(partition)) {

                int page = partitions.get(partition);

                int[] indices = cache.int2IntArr(id, PartitionedPager.ID2INTARR).get(page);

                List<String> list = datumDAO.list(query);

                result = list.subList(Math.min(list.size(), indices[0]), Math.min(list.size(), indices[1]));
            }
        }
        return result;
    }

    /**
     * Get multiple partitions.
     * @param query
     * @param partitionIds
     * @return
     */
    public Response partitions(Q query, List<String> partitionIds) {

        Map<String, List<String>> result = new HashMap<>();

        Map<Integer, int[]> pages = getPages(query);

        Map<String, Integer> partitions = getParitions(query);

        for(String partitionId : partitionIds) {
            List<String> partitionData = partition(query, partitionId);
            result.put(partitionId, partitionData);
        }

        return Response.status(Response.Status.OK).entity(
                result
        ).build();
    }


    public <U extends SqlUpdate<T>,T> Response update(final U update) {

        int n = datumDAO.update((Q)update);

        method52DAO.addKey(update.table(), update.key());

        return Response.status(Response.Status.OK).entity( n ).build();
    }


    /**
     * Attempt to optimise table by creating indexes on the query keys.
     * @param query
     * @return
     */
    public Response optimise(Q query) {

        method52DAO.optimiseTable((DatumQuery)query);

        return Response.status(Response.Status.OK).entity(
                ImmutableMap.of("message", "OK")
        ).build();
    }

    /**
     * Return counts for the given partitions ids.
     * @param query
     * @param partitionIds
     * @return
     */
    public Response counts(Q query, List<String> partitionIds) {

        List<Object[]> counts = new ArrayList<>(partitionIds.size());

        Map<Integer, int[]> pages = getPages(query);

        Map<String, Integer> partitions = getParitions(query);

        for(String partitionId : partitionIds) {
            Integer pageId = partitions.get(partitionId);
            int count = 0;
            if(pageId != null) {
                int[] indices = pages.get(pageId);
                count =  indices[1] - indices[0];
            }

            counts.add(new Object[]{partitionId, count});

        }

        return Response.status(Response.Status.OK).entity(
                counts
        ).build();
    }

    public Response chunkCounts(Q query) {

        Map<String, Integer> counts = getChunkCounts(query);

        List<Map> formatted = new ArrayList<>();

        for(Map.Entry<String, Integer> entry : counts.entrySet()) {
            formatted.add(ImmutableMap.of("id", entry.getKey(), "count", entry.getValue()));
        }

        return Response.status(Response.Status.OK).entity(
                formatted
        ).build();
    }


    /**
     * Get the page number for the given partition id.
     * @param query
     * @param id
     * @return
     */
    public Response partitionPage(Q query, Object id) {

        Map<String, Integer> partitions = getParitions(query);

        Integer pageId = partitions.get(id);

        return Response.status(Response.Status.OK).entity(
                pageId
        ).build();
    }

//    @POST
//    @Path("select-distinct")
//    public Response selectDistinct(SelectDistinct selectDistinct) throws SQLException {
//
//        String sql = selectDistinct.sql();
//
//        List<String> data = datumDAO.selectString(sql);
//
//        return Response.status(Response.Status.OK).entity(
//                data
//        ).build();
//    }


}
