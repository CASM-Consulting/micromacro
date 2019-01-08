package uk.ac.susx.shl.micromacro.db;


import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DatumWrapperMapper implements RowMapper<DatumWrapper> {

    @Override
    public DatumWrapper map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new DatumWrapper(rs.getLong(1), rs.getString(2));
    }
}
