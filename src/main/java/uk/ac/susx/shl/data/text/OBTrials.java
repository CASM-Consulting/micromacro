package uk.ac.susx.shl.data.text;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.susx.shl.data.Match;
import uk.ac.susx.shl.data.geo.GeoJsonKnowledgeBase;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.span.Span;
import uk.ac.susx.tag.method51.core.meta.span.Spans;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.*;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

/**
 * Created by sw206 on 15/05/2018.
 */
public class OBTrials {

    private final Map<LocalDate, List<SimpleDocument>> documents;

    private final GeoJsonKnowledgeBase lookup;

    private final Key<Spans<List<String>, Map>> placeMatchKey;

    private KeySet keys;


    public OBTrials(String geoJsonPath) throws IOException {
        documents = new HashMap<>();
        lookup = new GeoJsonKnowledgeBase(Paths.get(geoJsonPath));
        placeMatchKey = Key.of("placeNameMatch", RuntimeType.listSpans(Map.class));
        keys = KeySet.of(placeMatchKey);
    }

    public void load() {

        List<XML2Datum.Element> interestingElements = new ArrayList<>();

        interestingElements.add(new XML2Datum.Element("div0", ImmutableMap.of("type", "sessionsPaper"), "sessionsPaper").isContainer(true));

        interestingElements.add(new XML2Datum.Element("div1", ImmutableMap.of("type", "trialAccount"), "trialAccount").valueAttribute("id"));

        interestingElements.add(new XML2Datum.Element("p", ImmutableMap.of(), "statement"));

        Path start = Paths.get("data", "sessionsPapersSample2");



        try {

            for(Datum trial : XML2Datum.getData(start, interestingElements, "trialAccount", "-id") ) {

                KeySet keys = trial.getKeys();
                Key<Spans<String, String>> sentenceKey = keys.get("statement");
                Key<String> textKey = keys.get("text");

                Key<String> idKey = trial.getKeys().get("trialAccount-id");

                List<Datum> statements = new ArrayList<>();

                KeySet retain = keys.with(idKey);

                Key<List<String>> tokenKey = null;
                Key<Spans<List<String>, String>> spansKey = null;

                for(Datum statement : trial.getSpannedData(sentenceKey, retain)) {

                    Datum tokenized = Tokenizer.tokenize(statement, textKey, retain);

                    KeySet tokenizedKeys = tokenized.getKeys();

                    tokenKey = tokenizedKeys.get(textKey+Tokenizer.SUFFIX);

                    spansKey = Key.of("placeName", RuntimeType.listSpans(String.class));

                    NER2Datum ner2Datum = new NER2Datum (
                        tokenKey,
                        ImmutableSet.of("placeName"),
                        spansKey,
                        true
                    );

                    String text = String.join(" ", tokenized.get(tokenKey));

                    String ner = NERSocket.get(text);

                    Datum nerd = ner2Datum.toDatum(ner);

                    if(tokenized.get(tokenKey).size() != nerd.get(tokenKey).size()) {

                        System.err.println("tokenised mismatch! Expected " + tokenized.get(tokenKey).size() + " got " + nerd.get(tokenKey).size());
                    } else {

                        tokenized = tokenized.with(nerd.getKeys().get("placeName"), nerd.get(spansKey));

                        statements.add(tokenized);

                        keys = keys
                                .with(tokenizedKeys)
                                .with(spansKey);
                    }
                }

                if(!statements.isEmpty()) {

                    String id = trial.get("trialAccount-id");

                    System.out.println(id);

                    ListIterator<Datum> itr = statements.listIterator();
                    while( itr.hasNext() ) {
                        Datum statement = itr.next();
                        Spans<List<String>, String> spans = statement.get(spansKey);

                        Spans<List<String>, Map> matchSpans = Spans.annotate(tokenKey, Map.class);

                        for (Span<List<String>, String> span : spans) {

                            String candidate = String.join(" ", span.getSpanned(statement) );

                            System.out.println(candidate);

                            List<Match> matches = lookup.getMatches(candidate);

                            if(!matches.isEmpty()) {

                                Match match = matches.get(0);

                                Span<List<String>, Map> matchSpan = Span.annotate(tokenKey, span.from(), span.to(), match.getMetadata());

                                matchSpans = matchSpans.with(matchSpan);
                            }
                        }

                        statement = statement.with(placeMatchKey, matchSpans);

                        itr.set(statement);
                    }

                    Datum2SimpleDocument<?> datum2SimpleDocument = new Datum2SimpleDocument(tokenKey, ImmutableList.of(spansKey));

                    SimpleDocument document = datum2SimpleDocument.toDocument(id, statements);

                    LocalDate date = getDate(trial.get("trialAccount-id"));

                    if(!documents.containsKey(date)) {
                        documents.put(date, new ArrayList<>());
                    }
                    documents.get(date).add(document);
                }
            }

        } catch (Throwable err) {
            err.printStackTrace ();
        }
    }

    public Map<LocalDate, List<SimpleDocument>> getDocumentsByTime() {
        return documents;
    }

    public KeySet keys() {
        return keys;
    }

    private LocalDate getDate(String id) {
        LocalDate date = LocalDate.parse(id.split("-")[0], new DateTimeFormatterBuilder()
                .appendLiteral('t')
                .appendValue(YEAR, 4)
                .appendValue(MONTH_OF_YEAR, 2)
                .appendValue(DAY_OF_MONTH, 2)
                .toFormatter());
        return date;
    }


    public static void main(String[] args ) throws Exception {
//        new OBTrials("LL_PL_PA_WA_POINTS_FeaturesT.json").load();
        LocalDate date = LocalDate.parse("t18120219-65".split("-")[0], new DateTimeFormatterBuilder()
                .appendLiteral('t')
                .appendValue(YEAR, 4)
                .appendValue(MONTH_OF_YEAR, 2)
                .appendValue(DAY_OF_MONTH, 2)
                .toFormatter());
        System.out.println(date.toString());
    }
}
