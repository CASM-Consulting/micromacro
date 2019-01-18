package uk.ac.susx.shl.micromacro.jdbi;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.ac.susx.shl.micromacro.api.DatumRep;
import uk.ac.susx.shl.micromacro.core.MalformedDatumException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class DatumRepMapper implements RowMapper<DatumRep> {

    @Override
    public DatumRep map(ResultSet rs, StatementContext ctx) throws SQLException {

        try {
            Map data = new ObjectMapper().readValue(rs.getString(2), Map.class);
            return new DatumRep(rs.getLong(1), data);
        } catch (IOException e) {
            throw new MalformedDatumException();
        }

    }
}
