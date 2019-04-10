package uk.ac.susx.shl.micromacro.jdbi;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlFragment;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlQuery;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BaseDAO<T, Q extends SqlQuery> implements DAO<T,Q> {

    private final Jdbi jdbi;
    private final RowMapper<T> mapper;

    public BaseDAO(Jdbi jdbi, RowMapper<T> mapper) {
        this.jdbi = jdbi;
        this.mapper = mapper;
    }

    @Override
    public Stream<T> stream(Q query){
        return jdbi.withHandle(handle -> handle.createQuery(query.sql())
                .map(mapper)
                .stream()
        );
    }

    @Override
    public int update(Q query){

        String sql = query.sql();

        return jdbi.withHandle( handle -> handle.createUpdate(sql).execute() );
    }

    @Override
    public List<T> list(Q query)  {
        Stream<T> stream = stream(query);
        List<T> list = stream.collect(Collectors.toList());
        stream.close();
        return list;
    }
}
