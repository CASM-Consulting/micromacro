package uk.ac.susx.shl.micromacro.jdbi;

import org.mapdb.DB;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface StreamIntercept<Q, O, T> {

    BiFunction<Q, O, Function<T, T>> getFn();
}
