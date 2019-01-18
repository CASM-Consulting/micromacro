package uk.ac.susx.shl.micromacro.jdbi;


import com.google.gson.Gson;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.KeySet;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DatumMapper implements RowMapper<Datum> {

    public final Gson gson;

    public DatumMapper(KeySet keys) {

        gson = GsonBuilderFactory.get(keys).create();
    }

    @Override
    public Datum map(ResultSet rs, StatementContext ctx) throws SQLException {

        long id = rs.getLong(1);
        String json = rs.getString(2);

        Datum datum = gson.fromJson(json, Datum.class);

        return datum;
    }
}
