package uk.ac.susx.shl.micromacro.jdbi;

import com.google.gson.Gson;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import uk.ac.susx.shl.micromacro.api.DatumRep;
import uk.ac.susx.tag.method51.core.data.store2.query.CreateIndex;
import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;
import uk.ac.susx.tag.method51.core.data.store2.query.Index;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class DatumDAO {

    private final Jdbi jdbi;
    private final Method52DAO method52DAO;

    public DatumDAO(Jdbi jdbi, Method52DAO method52DAO) {
        this.jdbi = jdbi;
        this.method52DAO = method52DAO;
    }

    public <T extends DatumQuery> List<Datum> execute(T query) throws SQLException {

        String table = query.table();

        KeySet keys = method52DAO.schema(table);

        String sql = query.sql();

        return jdbi.withHandle(handle -> handle.createQuery(sql)
            .map(new DatumMapper(keys))
            .list()
        );
    }

    public <T extends DatumQuery> int executeUpdate(T query) throws SQLException {

        String sql = query.sql();

        return jdbi.withHandle( handle -> handle.createUpdate(sql).execute() );
    }


    public <T extends DatumQuery> Stream<DatumRep> execute2Rep(T query) {
        KeySet keys = method52DAO.schema(query.table());
        return jdbi.withHandle(handle -> {
                    return handle.createQuery(query.sql())
                            .map(new DatumRepMapper(keys))
                            .stream();
                }
        );
    }


    public <T extends DatumQuery> Stream<String> execute2String(T query) {
        return jdbi.withHandle(handle -> handle.createQuery(query.sql())
                .map(new DatumStringMapper())
                .stream()
        );
    }

    public <T extends DatumQuery> Function<String, Datum> string2Datum(T query) {
        KeySet keys = method52DAO.schema(query.table());
        Gson gson = GsonBuilderFactory.get(keys).create();

        return (str) -> gson.fromJson(str, Datum.class);
    }


    public Function<String, Map> string2Map() {
        Gson gson = GsonBuilderFactory.get().create();
        return (str) -> gson.fromJson(str, Map.class);
    }

    public List<String> selectString(String sql) {

        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .mapTo(String.class)
                .list()
        );
    }

    public <T extends DatumQuery> void optimiseTable(T query) {

        String table = query.table();

        for(Index index : query.indexHints()) {

            CreateIndex createIndex =  new CreateIndex(table, index);

            jdbi.withHandle(handle -> handle.execute(createIndex.sql()));
        }
    }

    public void addKey(String table, Key key) {
        method52DAO.addKey(table, key);
    }
}

