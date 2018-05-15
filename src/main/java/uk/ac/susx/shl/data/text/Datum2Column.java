package uk.ac.susx.shl.data.text;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.span.Span;
import uk.ac.susx.tag.method51.core.meta.span.Spans;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class Datum2Column<T> {

    private Datum datum;
    private final Key<String> textKey;
    private final Key<Spans<String,String>> tokenKey;
//    private final SentenceDetector sentenceDetector;
    private final Tokenizer tokenizer;

    private final Key<Spans<String,T>> extractKey;

    public Datum2Column(Datum datum, Key<String> textKey, Key<Spans<String,T>> extractKey) throws IOException {
        this.datum = datum;
        this.textKey = textKey;
        this.extractKey = extractKey;
        tokenKey = Key.of("tokens", RuntimeType.stringSpans(String.class));

//        SentenceModel sentModel = new SentenceModel(Files.newInputStream(Paths.get("en-sent.bin")));
//        sentenceDetector = new SentenceDetectorME(sentModel);

        TokenizerModel tokenModel = new TokenizerModel(Files.newInputStream(Paths.get("en-token.bin")));
        tokenizer = new TokenizerME(tokenModel);
//        tokenizer = WhitespaceTokenizer.INSTANCE;

//        sentences();
        columnise();

    }

//    private void sentences() {
//
//        String text = datum.get(textKey);
//        Span[] sents = sentenceDetector.sentPosDetect(text);
//
//        Spans<String, String> spans = Spans.annotate(textKey, String.class);
//
//        for(Span sent : sents) {
//
//            int start = sent.getStart();
//            int end = sent.getEnd();
//
//            String s = text.substring(start, end);
//
//            spans = spans.with(start, end, "sentence");
//        }
//    }

    private void columnise() {

        String text = datum.get(textKey);
        opennlp.tools.util.Span[] tokeniserSpans = tokenizer.tokenizePos(text);

        Spans<String, String> tokenSpans = Spans.annotate(textKey, String.class);

        for(opennlp.tools.util.Span token : tokeniserSpans) {

            int start = token.getStart();
            int end = token.getEnd();

            String t = text.substring(start, end);



            tokenSpans = tokenSpans.with(start, end, "token");
        }

        datum = datum.with(tokenKey, tokenSpans);

        Datum listVersion = datum.stringSpans2List(tokenKey, KeySet.of(extractKey) );

        KeySet listKeys = listVersion.getKeys();

        Key<List<String>> listKey = listKeys.get(textKey.name()+"-list");
        Key<Spans<List<String>,T>> listExtractKey = listKeys.get(extractKey.name()+"-list");

        List<String> tokens = listVersion.get(listKey);

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < tokens.size(); ++i) {
            sb.append(tokens.get(i));
            sb.append("\t");
            Optional<Span<List<String>,T>> maybeSpan = listVersion.get(listExtractKey).getAt(i);
            if(maybeSpan.isPresent()) {
                Span<List<String>, T> span = maybeSpan.get();
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

        System.out.println(sb.toString());
    }
}
