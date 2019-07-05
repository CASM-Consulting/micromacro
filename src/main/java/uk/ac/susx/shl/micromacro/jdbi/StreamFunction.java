package uk.ac.susx.shl.micromacro.jdbi;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface StreamFunction<Q, T> extends BiFunction<Q, Object, Function<T, T>> {

    void end();
}
