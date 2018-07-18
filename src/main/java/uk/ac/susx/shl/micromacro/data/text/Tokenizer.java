package uk.ac.susx.shl.micromacro.core.data.text;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.span.Spans;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by sw206 on 15/05/2018.
 */
public class Tokenizer {

    private static opennlp.tools.tokenize.Tokenizer tokenizer;

    public synchronized static opennlp.tools.tokenize.Tokenizer get() {
        if(tokenizer ==  null) {
            try {
                TokenizerModel tokenModel = new TokenizerModel(Files.newInputStream(Paths.get("en-token.bin")));
                tokenizer = new TokenizerME(tokenModel);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return tokenizer;
    }

    public final static String SUFFIX = "-token";
    public final static Key<Spans<String,String>> TOKEN_KEY = Key.of("tokens", RuntimeType.stringSpans(String.class));

    public static Datum tokenize(Datum datum, Key<String> textKey, KeySet retain) {
        String text = datum.get(textKey);
        opennlp.tools.util.Span[] tokeniserSpans = get().tokenizePos(text);

        Spans<String, String> tokenSpans = Spans.annotate(textKey, String.class);

        for(opennlp.tools.util.Span token : tokeniserSpans) {

            int start = token.getStart();
            int end = token.getEnd();

//            String t = text.substring(start, end);

            tokenSpans = tokenSpans.with(start, end, "token");
        }

        datum = datum.with(TOKEN_KEY, tokenSpans);

        Datum tokenized = datum.stringSpans2List(TOKEN_KEY, retain, SUFFIX);

        return tokenized;
    }
}
