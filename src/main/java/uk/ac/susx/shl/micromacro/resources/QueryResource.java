package uk.ac.susx.shl.micromacro.resources;


import uk.ac.susx.shl.micromacro.core.QueryResultCache;
import uk.ac.susx.shl.micromacro.jdbi.CachingDAO;
import uk.ac.susx.shl.micromacro.jdbi.DAO;
import uk.ac.susx.shl.micromacro.jdbi.Method52DAO;
import uk.ac.susx.shl.micromacro.jdbi.PartitionPager;
import uk.ac.susx.tag.method51.core.data.store2.query.*;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class QueryResource<Q extends SqlQuery, U extends SqlUpdate> extends BaseDAOResource<String, Q> {

    private static final Logger LOG = Logger.getLogger(QueryResultCache.class.getName());

    private final Method52DAO method52DAO;

    public QueryResource(DAO<String, Q> datumDAO, Method52DAO method52DAO) {
        super(datumDAO);
        this.method52DAO = method52DAO;
    }

    public void query(AsyncResponse asyncResponse, Q query) throws Exception {
        daoStreamResponse(asyncResponse, query, (stream-> StreamSupport.stream(stream.spliterator(), false) ));
    }

    public void cacheOnly(AsyncResponse asyncResponse, Q query) throws Exception {
        daoStreamResponse(asyncResponse, query, Stream::count);
    }

    public void skipLimit(AsyncResponse asyncResponse, int skip, int limit, Q query) throws Exception {

        daoStreamResponse(asyncResponse, query, (stream-> {
            List<String> list = datumDAO.list(query);
            return list.subList(Math.min(list.size(), skip), Math.min(list.size(), skip + limit)).stream();
        }));
    }

    public void page(AsyncResponse asyncResponse, int page, Q query) throws Exception {
        daoStreamResponse(asyncResponse, query, (stream-> {
                CachingDAO<String, SqlQuery> cache = (CachingDAO<String, SqlQuery>)datumDAO.getDAO();

                int[] indices = cache.int2IntArr(cache.getQueryId(query), PartitionPager.ID2INTARR).get(page);

                List<String> list = datumDAO.list(query);

                return list.subList(Math.min(list.size(), indices[0]), Math.min(list.size(), indices[1])).stream();
        }));

    }

    public void partition(AsyncResponse asyncResponse, String partition, Q query) throws Exception {

        daoStreamResponse(asyncResponse, query, (stream-> {
            CachingDAO<String, SqlQuery> cache = (CachingDAO<String, SqlQuery>)datumDAO.getDAO();

            String id = cache.getQueryId(query);

            Map<String, Integer> partitions = cache.str2Int(id, PartitionPager.ID2PAGE);

            if(partitions.containsKey(partition)) {

                int page = partitions.get(partition);

                int[] indices = cache.int2IntArr(id, PartitionPager.ID2INTARR).get(page);

                List<String> list = datumDAO.list(query);

                return list.subList(Math.min(list.size(), indices[0]), Math.min(list.size(), indices[1])).stream();
            } else {
                return new ArrayList<>();
            }

        }));
    }

    public <U extends SqlUpdate<T>,T> Response update(final U update) {

        int n = datumDAO.update((Q)update);

        method52DAO.addKey(update.table(), update.key());

        return Response.status(Response.Status.OK).entity( n ).build();
    }

    public Response optimise(Q query) {

        method52DAO.optimiseTable((DatumQuery)query);

        return Response.status(Response.Status.OK).entity(
                "OK"
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
