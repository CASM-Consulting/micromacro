package uk.ac.susx.shl.micromacro.core.data.text;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
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
import java.util.List;

/**
 * Created by sw206 on 15/05/2018.
 */
public class Sentizer {

    private static SentenceDetector sentizer;

    public synchronized static SentenceDetector get() {
        if(sentizer ==  null) {
            try {
                SentenceModel sentModel = new SentenceModel(Files.newInputStream(Paths.get("en-sent.bin")));
                sentizer = new SentenceDetectorME(sentModel);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return sentizer;
    }

    public final static String SUFFIX = "-sent";
    public final static Key<Spans<String,Integer>> SENT_KEY = Key.of("sents", RuntimeType.stringSpans(Integer.class));

    public static Datum annotateSents(Datum datum, Key<String> textKey) {
        String text = datum.get(textKey);
        opennlp.tools.util.Span[] sentizerSpans = get().sentPosDetect(text);

        Spans<String, Integer> sentSpans = Spans.annotate(textKey, Integer.class);

        int i = 0;
        for(opennlp.tools.util.Span sent : sentizerSpans) {

            int start = sent.getStart();
            int end = sent.getEnd();

//            String t = text.substring(start, end);

            sentSpans = sentSpans.with(start, end, i);
            ++i;
        }

        datum = datum.with(SENT_KEY, sentSpans);

        return datum;
    }

    public static List<Datum> sentize(Datum datum, Key<String> textKey, KeySet retain) {

        datum = annotateSents(datum, textKey);

        List<Datum> sentences = datum.getSpannedData(SENT_KEY, retain, SUFFIX);

        return sentences;
    }


}
