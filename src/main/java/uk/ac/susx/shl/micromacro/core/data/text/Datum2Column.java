package uk.ac.susx.shl.micromacro.core.data.text;

import com.google.common.base.Optional;

import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.span.Span;
import uk.ac.susx.tag.method51.core.meta.span.Spans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Datum2Column {

    private final Datum datum;
    private final Key textKey;

    private final List<Key> extractKeys;

    public Datum2Column(Datum datum, Key textKey, List<Key> extractKeys) throws IOException {
        this.datum = datum;
        this.textKey = textKey;
        this.extractKeys = extractKeys;
    }


    public String columnise() {

        Datum listVersion;
        Key<List<String>> listKey;
        KeySet listKeys;
        String suffix;
        if(!textKey.type.type.getRawClass().isAssignableFrom(List.class)) {

            suffix = Tokenizer.SUFFIX;
            listVersion = Tokenizer.tokenize(datum, textKey, KeySet.ofIterable(new ArrayList<>(extractKeys)));
            listKeys = listVersion.getKeys();
            listKey = listKeys.get(textKey.name()+suffix);
        } else {

            suffix = "";
            listVersion = datum;
            listKeys = datum.getKeys();
            listKey = textKey;
        }




        List<String> tokens = listVersion.get(listKey);

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < tokens.size(); ++i) {
            sb.append(tokens.get(i));
            sb.append("\t");

            List<String> label = new ArrayList<>();

            for(Key extractKey : extractKeys) {
                Key<Spans<List<String>,?>> listExtractKey = listKeys.get(extractKey.name()+suffix);

                Optional<Spans<List<String>,?>> maybeSpans = listVersion.maybeGet(listExtractKey);

                if(maybeSpans.isPresent() && maybeSpans.get().mayberGetAt(i).isPresent()) {

                    Span<List<String>,?> span = maybeSpans.get().getAt(i);

                    if(span.from() == i) {
                        label.add(span.get() + "-B");
                    } else {
                        label.add(span.get() + "-I");
                    }

                }
//                else {
//                    sb.append("O");
//                }
            }

            int ls = label.size();
            if(ls == 0) {
                sb.append("O");
            } else if( ls == 1 ) {
                sb.append(label.get(0));
            } else {
                throw new RuntimeException("overlapping labels, can't deal.");
            }

            sb.append("\n");


        }

        return sb.toString();
    }
}
