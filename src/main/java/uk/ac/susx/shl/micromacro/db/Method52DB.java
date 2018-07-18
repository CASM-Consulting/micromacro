package uk.ac.susx.shl.micromacro.db;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.UseRowMapper;

/**
 * Created by sw206 on 18/07/2018.
 */
public interface Method52DB {

    @SqlQuery("select id, data from <table> where data->'<id_key>'= :id")
    String trialById(@Define("table") String table, @Define("id_key") String idKey, @Bind("id") int id);

}
