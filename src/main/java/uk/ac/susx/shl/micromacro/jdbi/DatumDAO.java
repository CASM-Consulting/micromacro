package uk.ac.susx.shl.micromacro.jdbi;

import org.jdbi.v3.core.Jdbi;
import uk.ac.susx.shl.micromacro.api.DatumRep;
import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.KeySet;

import java.sql.SQLException;
import java.util.List;

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


    public <T extends DatumQuery> List<DatumRep> execute2Rep(T query) {
        return jdbi.withHandle(handle -> handle.createQuery(query.sql())
                .map(new DatumRepMapper())
                .list()
        );
    }


    public List<String> selectString(String sql) {

        return jdbi.withHandle(handle -> handle.createQuery(sql)
                .mapTo(String.class)
                .list()
        );
    }

}

