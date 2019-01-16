package uk.ac.susx.shl.micromacro.jdbi;

import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class DatumWrapperDAO {

    private final Jdbi jdbi;

    public DatumWrapperDAO(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<DatumWrapper> select(String sql) {
        return jdbi.withHandle(handle -> handle.createQuery(sql)
            .map(new DatumWrapperMapper())
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

