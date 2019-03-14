package uk.ac.susx.shl.micromacro.jdbi;


import com.google.gson.Gson;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.ac.susx.shl.micromacro.api.DatumRep;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DatumStringMapper implements RowMapper<String> {

    @Override
    public String map(ResultSet rs, StatementContext ctx) throws SQLException {

        return rs.getString(2);
    }
}
