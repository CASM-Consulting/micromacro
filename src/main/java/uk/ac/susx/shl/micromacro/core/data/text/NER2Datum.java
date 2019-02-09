package uk.ac.susx.shl.micromacro.core.data.text;

import com.google.common.collect.ImmutableSet;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.span.Span;
import uk.ac.susx.tag.method51.core.meta.span.Spans;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NER2Datum {


    private final Key<String> textKey;
    private final Key<Spans<String,String>> tokensKey;
    private final ImmutableSet<String> labels;
    private final Key<Spans<Spans<String,String>, String>> spansKey;
    private final boolean iob;

    public NER2Datum(Key<String> textKey, Key<Spans<String,String>> tokensKey, Key<Spans<Spans<String,String>, String>> spansKey,  Set<String> labels, boolean iob) {
        this.textKey = textKey;
        this.tokensKey = tokensKey;
        this.labels = ImmutableSet.copyOf(labels);
        this.spansKey = spansKey;
        this.iob = iob;
    }

    public Datum toDatum(Datum datum, String ner) {
        return toDatum(datum, ner, labels, spansKey);
    }

    /**
     *
     * @param datum original datum
     * @param ner the ner'd string [token]/[iob-][label]
     * @param labels valid ner labels
     * @param spansKey output spans
     * @return
     */

    public Datum toDatum(Datum datum, String ner, Set<String> labels, Key<Spans<Spans<String,String>, String>> spansKey) {


        Spans<Spans<String,String>, String> spans = Spans.annotate(tokensKey, String.class);

        String[] chunks = ner.split(" ");

        List<String> tokens = new ArrayList<>();

        Optional<Integer> from = Optional.empty();
        String chunkLabel = null;

        int i = 0;
        for(String chunk : chunks) {

            int idx = chunk.lastIndexOf("/");

            if(idx == -1 && chunk.contains("\n")) {
                continue;
            } else if(idx == -1) {

                System.err.println("Wrong number of chunk bits! " + chunk);
                continue;
            }

            String token = chunk.substring(0,idx);
            String label = chunk.substring(idx+1);

            String l = (iob ? label.replace("-B", "") : label);


            if(from.isPresent()) {
                l = (iob ? label.replace("-I", "") : label);
                if(!labels.contains(l)) {
                    Span<Spans<String,String>, String> span = Span.annotate(tokensKey, from.get(), i, chunkLabel);
                    spans = spans.with(span);
                    from = Optional.empty();
                }
            } else if(labels.contains(l) ) {
                from = Optional.of(i);
                chunkLabel = l;
            }

            tokens.add(token);

            ++i;
        }

        datum = datum
            .with(spansKey, spans);


        return datum;
    }
}
