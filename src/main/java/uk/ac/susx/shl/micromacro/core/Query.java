package uk.ac.susx.shl.micromacro.core;

import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Query<T extends DatumQuery> implements Serializable {

    private final List<T> history;

    public Query() {
        history = new ArrayList<>();
    }

    public Query add(T query) {
        history.add(query);
        return this;
    }


}
