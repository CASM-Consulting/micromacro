package uk.ac.susx.shl.data.text;

import com.google.common.collect.ImmutableList;
import uk.ac.susx.tag.method51.core.collections.ImmutableMap;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.span.Spans;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import java.util.*;

public class Document {

    private final ImmutableList<Datum> data;
    private final Key<List<String>> textKey;
    private final Key<String> idKey;
    private final ImmutableMap<String, Key<Spans<List<String>, String>>> spansKeys;

    public static class Span {
        public final int from;
        public final int to;
        public final String value;

        public Span(int f, int t, String v) {
            from = f;
            to = t;
            value = v;
        }
    }

    public Document(String textKey, String idKey, String... spans) {
        this.textKey = Key.of(textKey, RuntimeType.list(RuntimeType.STRING));

        Map<String, Key<Spans<List<String>, String>>> keys = new HashMap<>();

        for(String span : spans) {
            keys.put(span, Key.of(span, RuntimeType.listSpans(String.class)));
        }
        spansKeys = ImmutableMap.of(keys);
        data = ImmutableList.of();
        this.idKey = Key.of(idKey, RuntimeType.STRING);
    }

    public Document(Key<List<String>> textKey, Key<String> idKey, Key<Spans<List<String>, String>>... spansKeys) {

        this.textKey = textKey;

        Map<String, Key<Spans<List<String>, String>>> keys = new HashMap<>();
        for(Key<Spans<List<String>, String>> span : spansKeys) {
            keys.put(span.toString(), span);
        }
        this.spansKeys = ImmutableMap.of(keys);
        data = ImmutableList.of();
        this.idKey = idKey;
    }

    private Document(Key<String> idkey, ImmutableList<Datum> data, Key<List<String>> dataKey, ImmutableMap<String, Key<Spans<List<String>, String>>> spansKeys) {
        this.idKey = idkey;
        this.data = data;
        this.textKey = dataKey;
        this.spansKeys = spansKeys;
    }

    public Document with(List<String> tokens, Map<String, List<Span>> spans) {

        Datum datum = new Datum();
        datum = datum.with(textKey, tokens);
        Spans<List<String>, String> ss = Spans.annotate(textKey, String.class);

        for(Map.Entry<String, List<Span>> entry : spans.entrySet()) {
            String s = entry.getKey();
            for(Span span : entry.getValue()) {

                ss = ss.with(span.from, span.to, span.value);
            }
            datum = datum.with(spansKeys.get(s), ss);
        }

        return new Document(idKey, new ImmutableList.Builder<Datum>().addAll(data).add(datum).build(), textKey, spansKeys);
    }

    public Document with(Datum datum) {
        //check for required keys?
        return new Document(idKey, new ImmutableList.Builder<Datum>().addAll(data).add(datum).build(), textKey, spansKeys);
    }

    public Document with(List<Datum> data) {
        //check for required keys?
        return new Document(idKey, new ImmutableList.Builder<Datum>().addAll(this.data).addAll(data).build(), textKey, spansKeys);
    }

    public List<List<Candidate>> getCandidates(String spanType) {
        List<List<Candidate>> candidatess = new ArrayList<>();

        for(Datum datum : data) {
            List<Candidate> candidates = new ArrayList<>();

            for(uk.ac.susx.tag.method51.core.meta.span.Span<List<String>, String> span : datum.get(spansKeys.get(spanType)).get() ) {
                String text = String.join(" ", span.getSpanned(datum));
                candidates.add(Candidate.of(text));
            }

            candidatess.add(candidates);
        }

        return candidatess;
    }

    public List<Datum> getData() {
        return data;
    }

    public int size() {
        return data.size();
    }

}
