package uk.ac.susx.shl.micromacro.jdbi;

import uk.ac.susx.tag.method51.core.data.store2.query.SqlQuery;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public interface DAO<T, Q extends SqlQuery> {

    default DAO<T,Q> getDAO() {
        return this;
    }

    default Stream<T> stream(Q query, BiFunction<Q, Object, Function<T, T>>... functions) {
        return stream(query);
    }

    default Stream<T> stream(Q query) {
        return stream(query, (q, thing)->s->s);
    };

    List<T> list(Q query);

    int update(Q query);

}
