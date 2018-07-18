package uk.ac.susx.shl.micromacro.core.data.text;

import com.google.common.base.Optional;

import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.span.Span;
import uk.ac.susx.tag.method51.core.meta.span.Spans;

import java.io.IOException;
import java.util.List;

public class Datum2Column<T> {

    private Datum datum;
    private final Key<String> textKey;
//    private final SentenceDetector sentenceDetector;

    private final Key<Spans<String,T>> extractKey;

    public Datum2Column(Datum datum, Key<String> textKey, Key<Spans<String,T>> extractKey) throws IOException {
        this.datum = datum;
        this.textKey = textKey;
        this.extractKey = extractKey;

    }


    public String columnise() {

        Datum listVersion = Tokenizer.tokenize(datum, textKey, KeySet.of(extractKey));

        KeySet listKeys = listVersion.getKeys();

        Key<List<String>> listKey = listKeys.get(textKey.name()+"-list");
        Key<Spans<List<String>,T>> listExtractKey = listKeys.get(extractKey.name()+"-list");

        List<String> tokens = listVersion.get(listKey);

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < tokens.size(); ++i) {
            sb.append(tokens.get(i));
            sb.append("\t");
            Optional<Spans<List<String>,T>> maybeSpans = listVersion.maybeGet(listExtractKey);
            if(maybeSpans.isPresent() && maybeSpans.get().mayberGetAt(i).isPresent()) {

                Span<List<String>,T> span = maybeSpans.get().getAt(i);
                sb.append(span.get());

                if(span.from() == i) {
                    sb.append("-B");
                } else {
                    sb.append("-I");
                }

            } else {
                sb.append("O");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
