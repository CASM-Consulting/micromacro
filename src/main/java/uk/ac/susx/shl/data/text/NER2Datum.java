package uk.ac.susx.shl.data.text;

import com.google.common.collect.ImmutableSet;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.span.Span;
import uk.ac.susx.tag.method51.core.meta.span.Spans;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NER2Datum {


    private final Key<List<String>> textKey;
    private final ImmutableSet<String> labels;
    private final Key<Spans<List<String>, String>> spansKey;
    private final boolean iob;

    public NER2Datum(Key<List<String>> textKey, Set<String> labels, Key<Spans<List<String>, String>> spansKey, boolean iob) {
        this.textKey = textKey;
        this.labels = ImmutableSet.copyOf(labels);
        this.spansKey = spansKey;
        this.iob = iob;
    }


    public Datum toDatum(String ner) {

        Datum datum = new Datum();

        Spans<List<String>, String> spans = Spans.annotate(textKey, String.class);

        String[] chunks = ner.split(" ");

        List<String> tokens = new ArrayList<>();

        Optional<Integer> from = Optional.empty();

        int i = 0;
        for(String chunk : chunks) {

            String[] bits = chunk.split("/");
            if(bits.length ==1 && bits[0].equals("\n")) {
                continue;
            } else if(bits.length != 2) {

                System.err.println("Wrong number of chunk bits!");
                continue;
            }

            String token = bits[0];
            String label = bits[1];

            String l = (iob ? label.replace("-B", "") : label);

            if(from.isPresent()) {
                if(!labels.contains(l)) {
                    Span<List<String>, String> span = Span.annotate(textKey, from.get(), i, l);
                    spans = spans.with(span);
                }
            } else if(labels.contains(l) ) {
                from = Optional.of(i);
            }

            tokens.add(token);

            ++i;
        }

        datum = datum
            .with(textKey, tokens)
            .with(spansKey, spans);


        return datum;
    }
}
