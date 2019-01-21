package uk.ac.susx.shl.micromacro.jdbi;


import com.google.gson.Gson;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;
import uk.ac.susx.tag.method51.twitter.LabelDecision;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class DatumMapper implements RowMapper<Datum> {

    private static final Logger LOG = Logger.getLogger(DatumMapper.class.getName());

    public final Gson gson;

    public DatumMapper(KeySet keys) {

        gson = GsonBuilderFactory.get(keys).create();
    }

    @Override
    public Datum map(ResultSet rs, StatementContext ctx) throws SQLException {

        long id = rs.getLong(1);
        String json = rs.getString(2);

        Datum datum = gson.fromJson(json, Datum.class);

        datum = labelDecision2Label(datum);

        return datum;
    }


    public static Datum labelDecision2Label(Datum datum) {

        Set<String> models = new HashSet<>();

        for(Key key : datum.getKeys().keys.keys()) {

            if(key.type.isAssignableFrom(RuntimeType.of(LabelDecision.class))) {

                LabelDecision l = (LabelDecision)datum.get(key);

                int idx = key.name.lastIndexOf(l.label);

                String model = key.name.substring(0,idx-1);

                if(!models.add(model)) {
                    LOG.warning("multiple label decisions for " + model);
                }

                datum = datum.without(key)
                    .with(Key.of(key.namespace, model, RuntimeType.STRING), l.label);
            }
        }

        return datum;
    }
}
