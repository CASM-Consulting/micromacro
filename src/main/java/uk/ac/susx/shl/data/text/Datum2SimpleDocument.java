package uk.ac.susx.shl.data.text;

import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.span.Span;
import uk.ac.susx.tag.method51.core.meta.span.Spans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Datum2SimpleDocument<T extends Object> {

    private final Key<List<String>> textKey;
    private final List<Key<Spans<List<String>, T>>> spansKeys;

    public Datum2SimpleDocument(Key<List<String>> textKey, List<Key<Spans<List<String>, T>>> spansKeys) {

        this.textKey = textKey;
        this.spansKeys = spansKeys;
    }

    public SimpleDocument toDocument(String id, List<Datum> data) {
        SimpleDocument document = new SimpleDocument(id);

        for(Datum datum : data) {

            List<String> tokens = datum.get(textKey);
            Map<String,List<SimpleDocument.Span>> spans = new HashMap<>();

            for(Key<Spans<List<String>, T>> spansKey : spansKeys) {
                String type = spansKey.toString();

                spans.put(type, new ArrayList<>());

                for(Span<List<String>, ?> span : datum.get(spansKey)) {
                    SimpleDocument.Span simpleSpan = new SimpleDocument.Span(span.from(), span.to(), span.get());
                    spans.get(type).add(simpleSpan);
                }
            }

            SimpleDocument.Sentence sentence = new SimpleDocument.Sentence(tokens, spans);
            document = document.with(sentence);
        }

        return document;
    }

}
