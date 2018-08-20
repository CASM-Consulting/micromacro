package uk.ac.susx.shl.micromacro.core.data.text;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SimpleDocument implements Serializable {
    static final long serialVersionUID = 43L;

    private final String id;
    private final ImmutableList<Sentence> data;
    private final ImmutableMap<String,String> metadata;


    public static class Sentence implements Serializable {
        static final long serialVersionUID = 43L;

        public final List<String> tokens;
        public final Map<String, List<Span>> spans;
        public Sentence(List<String> tokens, Map<String, List<Span>> spans) {
            this.tokens = tokens;
            this.spans = spans;
        }

    }

    public static class Span<T> implements Serializable {
        static final long serialVersionUID = 43L;

        public final int from;
        public final int to;
        public final T value;

        public Span(int f, int t, T v) {
            from = f;
            to = t;
            value = v;
        }
    }

    public SimpleDocument(String id, List<Sentence> data, Map<String,String> metadata) {
        this.id = id;
        this.data = ImmutableList.copyOf(data);
        this.metadata = ImmutableMap.copyOf(metadata);

    }

    public SimpleDocument(String id) {
        this.id = id;
        data = ImmutableList.of();
        metadata = ImmutableMap.of();
    }

    public SimpleDocument with(Sentence sentence) {
        return new SimpleDocument(id, new ImmutableList.Builder<Sentence>().addAll(data).add(sentence).build(), metadata);
    }

    public SimpleDocument with(String key, String value) {
        return new SimpleDocument(id, data,new ImmutableMap.Builder<String,String>().put(key, value).build());
    }

    public int size() {
        return data.size();
    }
}
