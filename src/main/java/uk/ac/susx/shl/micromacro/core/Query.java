package uk.ac.susx.shl.micromacro.core;

import uk.ac.susx.shl.micromacro.api.AbstractQueryRep;
import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Query<T extends DatumQuery> implements Serializable {

    private final Queue<T> history;

    public Query() {
        history = new LinkedList<>();
    }

    public Query add(T query) {
        history.add(query);
        return this;
    }

    public T get() {
        return history.peek();
    }

    public Queue<T> history() {
        return history;
    }



}
