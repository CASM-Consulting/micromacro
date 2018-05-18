package uk.ac.susx.shl.data.text;

import com.google.common.collect.ImmutableList;
import uk.ac.susx.tag.method51.core.collections.ImmutableMap;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.span.Spans;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleDocument {

    private final String id;
    private final ImmutableList<Sentence> data;

    public static class Sentence {
        public final List<String> tokens;
        public final Map<String, List<Span>> spans;
        public Sentence(List<String> tokens, Map<String, List<Span>> spans) {
            this.tokens = tokens;
            this.spans = spans;
        }

    }

    public static class Span<T> {
        public final int from;
        public final int to;
        public final T value;

        public Span(int f, int t, T v) {
            from = f;
            to = t;
            value = v;
        }
    }

    public SimpleDocument(String id, List<Sentence> data) {
        this.id = id;
        this.data = ImmutableList.copyOf(data);
    }

    public SimpleDocument(String id) {
        this.id = id;
        data = ImmutableList.of();
    }

    public SimpleDocument with(Sentence sentence) {
        return new SimpleDocument(id, new ImmutableList.Builder<Sentence>().addAll(data).add(sentence).build());
    }

    public int size() {
        return data.size();
    }
}
