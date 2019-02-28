package uk.ac.susx.shl.micromacro.core;

import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;

import java.io.Serializable;
import java.util.*;

public class Query<T extends DatumQuery> implements Serializable {

    private final LinkedList<T> history;

    private final Map<String, Object> metadata;

    public Query() {
        history = new LinkedList<>();
        metadata = new HashMap<>();
    }

    public Query(List<T> history, Map<String,Object> metadata) {
        this.history = new LinkedList<>(history);
        this.metadata = metadata;
    }

    public Query add(T query) {
        history.add(query);
        return this;
    }

    public T get() {
        return history.peekLast();
    }

    public T get(int i) {

        return history.get(history.size()-1+i);
    }

    public Query setMeta(String key, String value) {
        metadata.put(key, value);
        return this;
    }

    public Map<String, Object> getMeta() {
        return metadata;
    }


    public Queue<T> history() {
        return history;
    }
}
