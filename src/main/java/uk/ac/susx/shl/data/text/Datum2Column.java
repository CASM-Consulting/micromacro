package uk.ac.susx.shl.data.text;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.Span;

import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.span.Spans;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Datum2Column {

    private Datum datum;
    private final Key<String> textKey;
//    private final SentenceDetector sentenceDetector;
    private final Tokenizer tokenizer;

    private final Key<Spans<String, String>> sentenceKey;

    public Datum2Column(Datum datum, String textKey, List<String> contentkeys, List<String> spanKeys) throws IOException {
        this.datum = datum;
        this.textKey = Key.of(textKey, RuntimeType.STRING);
        sentenceKey = Key.of("sentence", RuntimeType.stringSpans(String.class));

//        SentenceModel sentModel = new SentenceModel(Files.newInputStream(Paths.get("en-sent.bin")));
//        sentenceDetector = new SentenceDetectorME(sentModel);

//        TokenizerModel tokenModel = new TokenizerModel(Files.newInputStream(Paths.get("en-token.bin")));
        tokenizer = WhitespaceTokenizer.INSTANCE;


//        sentences();
//        tokens();

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

    private void tokens() {

        String text = datum.get(textKey);
        Span[] sents = tokenizer.tokenizePos(text);

        Spans<String, String> spans = Spans.annotate(textKey, String.class);

        for(Span sent : sents) {

            int start = sent.getStart();
            int end = sent.getEnd();

            String t = text.substring(start, end);

            spans = spans.with(start, end, "token");
        }
    }
}
