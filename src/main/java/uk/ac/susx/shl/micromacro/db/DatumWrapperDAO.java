package uk.ac.susx.shl.micromacro.db;

import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class DatumWrapperDAO {

    private final Jdbi jdbi;

    public DatumWrapperDAO(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public List<DatumWrapper> execute(String sql) {
        return jdbi.withHandle(handle -> handle.createQuery(sql)
                    .map(new DatumWrapperMapper())
                    .list()
        );
    }

}

