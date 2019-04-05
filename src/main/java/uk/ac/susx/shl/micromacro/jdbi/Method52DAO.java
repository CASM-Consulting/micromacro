package uk.ac.susx.shl.micromacro.jdbi;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import uk.ac.susx.tag.method51.core.data.PostgreSQLConnection;
import uk.ac.susx.tag.method51.core.data.PostgresUtils;
import uk.ac.susx.tag.method51.core.data.StoreException;
import uk.ac.susx.tag.method51.core.data.impl.PostgreSQLDatumStore;
import uk.ac.susx.tag.method51.core.data.store2.query.CreateIndex;
import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;
import uk.ac.susx.tag.method51.core.data.store2.query.Index;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

/**
 * Created by sw206 on 12/09/2018.
 */
public class Method52DAO {

    private static final Logger LOG = Logger.getLogger(Method52DAO.class.getName());

    private final Jdbi jdbi;

    public Method52DAO(Jdbi jdbi) {

        this.jdbi = jdbi;
    }

    public List<String> listTables() throws SQLException {
        return jdbi.withHandle(handle -> {
            Connection con = handle.getConnection();
            //no need to close - hadle does it
            List<String> dbs = PostgresUtils.getTableNames(con);
            return dbs;
        });
    }

    public KeySet schema(String table) {

        Gson gson = GsonBuilderFactory.get().create();

        try (Handle handle  = jdbi.open()) {
            Connection con = handle.getConnection();
            //no need to close - handle does it
            JsonElement keysMeta = gson.fromJson(PostgresUtils.getComment(con, table), JsonObject.class).get("keys");

            KeySet keys = gson.fromJson(keysMeta, KeySet.class);

            return keys;
        } catch (SQLException e) {
            LOG.warning(e.getMessage());
            return KeySet.of();
        }
    }

    public KeySet addKey(String table, Key key) {

        Gson gson = GsonBuilderFactory.get().create();

        try (Handle handle  = jdbi.open()) {
            Connection con = handle.getConnection();
            //no need to close - hadle does it
            JsonObject tableMeta = gson.fromJson(PostgresUtils.getComment(con, table), JsonObject.class);

            KeySet keys = gson.fromJson(tableMeta.get("keys"), KeySet.class);

            keys = keys.with(key);

            tableMeta.add("keys", gson.toJsonTree(keys, KeySet.class));

            PostgresUtils.setComment(con, table, gson.toJson(tableMeta));

            return keys;
        } catch (SQLException e) {
            LOG.warning(e.getMessage());
            return KeySet.of();
        }
    }



    public <T extends DatumQuery> void optimiseTable(T query) {

        String table = query.table();

        for(Index index : query.indexHints()) {

            CreateIndex createIndex =  new CreateIndex(table, index);

            jdbi.withHandle(handle -> handle.execute(createIndex.sql()));
        }
    }

    
    public List<Datum>  getScores(String table,
                                  String trialIdKey,
                                  String sentenceIdKey,
                                  List<String> annotationKeys, List<String> ids) throws SQLException, StoreException {

        boolean convertIds = false;
        //change back to native id format
        if(convertIds) {
            ListIterator<String> itr = ids.listIterator();
            while(itr.hasNext()) {
                String id = itr.next();
                itr.set(id.replaceAll("-", "."));
            }
        }

        Gson gson = GsonBuilderFactory.get().create();

        try (Handle handle  = jdbi.open()) {
            Connection con = handle.getConnection();
            //no need to close - handle does it

            JsonElement keysMeta = gson.fromJson(PostgresUtils.getComment(con, table), JsonObject.class).get("keys");

            KeySet keys = gson.fromJson(keysMeta, KeySet.class);

            Key<String> _trialIdKey = keys.get(trialIdKey);

            Key<String> idKey = keys.get(sentenceIdKey);

            gson = GsonBuilderFactory.get(keys).create();

            KeySet getKeys = KeySet.of(idKey);

            for(String k : annotationKeys) {
                getKeys = getKeys.with(keys.get(k));
            }


            PostgreSQLConnection connectionParams = new PostgreSQLConnection().setConnection(con);
            PostgreSQLDatumStore store = new PostgreSQLDatumStore.Builder(connectionParams, table)
                    .incoming(getKeys)
                    .uniqueIndex(idKey)
                    .lookup(_trialIdKey)
                    .build();

            store.connect();

            List<Datum> results = store.get(ids);

            if(convertIds) {

                ListIterator<Datum> jtr = results.listIterator();
                while(jtr.hasNext()) {
                    Datum datum = jtr.next();
                    jtr.set(datum.with(idKey, datum.get(idKey).replaceAll("\\.", "-")));
                }
            }

            return results;
        }
    }
}
