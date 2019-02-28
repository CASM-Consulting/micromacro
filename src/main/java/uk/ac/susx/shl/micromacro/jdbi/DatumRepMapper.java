package uk.ac.susx.shl.micromacro.jdbi;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.dropwizard.jackson.Jackson;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.ac.susx.shl.micromacro.api.DatumRep;
import uk.ac.susx.shl.micromacro.core.MalformedDatumException;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DatumRepMapper implements RowMapper<DatumRep> {

    private final Set<String> keys;

    public DatumRepMapper(KeySet keys){
        this.keys = keys.keys.keySet().stream().map(Key::toString).collect(Collectors.toSet());
    }

    @Override
    public DatumRep map(ResultSet rs, StatementContext ctx) throws SQLException {

//        try {
//            Map data = new ObjectMapper().readValue(rs.getString(2), Map.class);
            Map<String, ?> rawData =  new Gson().fromJson(rs.getString(2), Map.class);

            Map data = new HashMap();
            for(Map.Entry<String,?> entry : rawData.entrySet()) {
                if(keys.contains(entry.getKey())) {
                    data.put(entry.getKey(),entry.getValue());
                }
            }

            return new DatumRep(rs.getLong(1), data);
//        } catch (IOException e) {
//            throw new MalformedDatumException();
//        }

    }
}
