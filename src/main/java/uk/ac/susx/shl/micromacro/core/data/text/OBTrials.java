package uk.ac.susx.shl.micromacro.core.data.text;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.jdbi.v3.core.Jdbi;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import uk.ac.susx.shl.micromacro.client.StanfordNER;
import uk.ac.susx.shl.micromacro.core.data.Match;
import uk.ac.susx.shl.micromacro.core.data.geo.GeoJsonKnowledgeBase;
import uk.ac.susx.tag.method51.core.data.impl.PostgreSQLDatumStore;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.span.Span;
import uk.ac.susx.tag.method51.core.meta.span.Spans;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

/**
 * Created by sw206 on 15/05/2018.
 */
public class OBTrials {

    private final Map<LocalDate, List<SimpleDocument>> trialsByDate;
    private final Map<String, SimpleDocument> trialsById;
    private final List<Map<String, String>> matches;

    private final GeoJsonKnowledgeBase lookup;

    private final Key<Spans<List<String>, Map>> placeMatchKey;

    private final Path start;

    private KeySet keys;

    private final DateTimeFormatter id2Date = new DateTimeFormatterBuilder()
                .appendLiteral('t')
                .appendValue(YEAR, 4)
                .appendValue(MONTH_OF_YEAR, 2)
                .appendValue(DAY_OF_MONTH, 2)
                .toFormatter();

    private final DateTimeFormatter file2Date = new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4)
            .appendValue(MONTH_OF_YEAR, 2)
            .appendValue(DAY_OF_MONTH, 2)
            .toFormatter();

    private final DateTimeFormatter date2JS = new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4)
            .appendLiteral('-')
            .appendValue(MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(DAY_OF_MONTH, 2)
            .toFormatter();


    public OBTrials(String sessionsPath, String geoJsonPath, String obMapPath) throws IOException {
        lookup = new GeoJsonKnowledgeBase(Paths.get(geoJsonPath));
        placeMatchKey = Key.of("placeNameMatch", RuntimeType.listSpans(Map.class));
        keys = KeySet.of(placeMatchKey);
        start = Paths.get(sessionsPath);

        DB db = DBMaker
                .fileDB(obMapPath)
                .fileMmapEnable()
                .closeOnJvmShutdown()
//                .readOnly()
                .make();

        trialsByDate =  (Map<LocalDate, List<SimpleDocument>>) db.hashMap("trials-by-date").createOrOpen();
        trialsById = (Map<String, SimpleDocument>) db.hashMap("trials-by-id").createOrOpen();
        matches = (List) db.indexTreeList("matches").createOrOpen();
    }

    public void clear() {
        trialsByDate.clear();
        trialsById.clear();
        matches.clear();
    }


    private Path getFile() {
        return null;
    }

    private Set<Path> getFiles(LocalDate from, LocalDate to) throws IOException{

        Set<Path> paths = new HashSet<>(Files.walk(start).filter(path -> {
            if(!path.toString().endsWith(".xml")) {
                return false;
            }

            String normalised = path.getFileName().toString().replaceAll("^(\\d{4}\\d{2}\\d{2})\\w?\\.xml", "$1");
            try {

                LocalDate fileDate = LocalDate.parse(normalised, file2Date);
                if(fileDate.isAfter(from) && fileDate.isBefore(to) || fileDate.equals(from) || fileDate.equals(to)) {
                    return true;
                } else {
                    return false;
                }
            } catch(DateTimeParseException e) {
                return false;
            }


        }).collect(Collectors.toList()));


        return paths;
    }

    /**
     * Marshals OB Corpus XML TEI through NER to DBMap indexes by id and date.
     *
     */
    public void load(LocalDate from, LocalDate to) {

        Key<Spans<String, String>> sessions = Key.of("sessionsPaper", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> trials = Key.of("trialAccount", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> stments = Key.of("statement", RuntimeType.stringSpans(String.class));
//        Key<Spans<String, String>> entities = Key.of("entities", RuntimeType.stringSpans(String.class));

        Map<Key<Spans<String, String>>, List<XML2Datum.Element>> interestingElements = new HashMap<>();


        interestingElements.put(sessions, ImmutableList.of(
                new XML2Datum.Element("div0", ImmutableMap.of("type", "sessionsPaper"), "sessionsPaper").isContainer(true)
        ));

        interestingElements.put(trials, ImmutableList.of(
                new XML2Datum.Element("div1", ImmutableMap.of("type", "trialAccount"), "trialAccount").valueAttribute("id")
        ));

        interestingElements.put(stments, ImmutableList.of(
                new XML2Datum.Element("p", ImmutableMap.of(), "statement")
        ));

//        interestingElements.add(new XML2Datum.Element("div1", ImmutableMap.of("type", "frontMatter"), "frontMatter"));
//        interestingElements.put(entities, ImmutableList.of(
////                new XML2Datum.Element("placeName", ImmutableMap.of(), "placeName"),
//                new XML2Datum.Element("rs", ImmutableMap.of("type", "crimeDate"), "crimeDate"))
//        );

//        List<XML2Datum.Element> interestingElements = new ArrayList<>();
//
//        interestingElements.add(new XML2Datum.Element("div0", ImmutableMap.of("type", "sessionsPaper"), "sessionsPaper").isContainer(true));
//
//        interestingElements.add(new XML2Datum.Element("div1", ImmutableMap.of("type", "trialAccount"), "trialAccount").valueAttribute("id"));
//
//        interestingElements.add(new XML2Datum.Element("p", ImmutableMap.of(), "statement"));

//        KeySet keys = XML2Datum.getKeys(interestingElements);

        try {

            Set<Path> files = getFiles(from, to);

            Iterator<Datum> itr = XML2Datum.getData(start, files, interestingElements, "trialAccount", "-id").iterator();

//            ForkJoinPool forkJoinPool = new ForkJoinPool(4);
//
//            forkJoinPool.submit(() -> {
//                Iterable<Datum> iterable = () -> itr;
            while(itr.hasNext()) {
                Datum trial = itr.next();

                String id = trial.get("trialAccount-id");

                if(trialsById.containsKey(id)) {
                    continue;
                }

                LocalDate date = getDate(id);
//                if(trialsByDate.containsKey(date)) {
//                    continue;
//                }

//                Stream<Datum> stream = StreamSupport.stream(iterable.spliterator(),true);

//                stream.forEach(trial -> {

                KeySet keys = trial.getKeys();
                Key<Spans<String, String>> sentenceKey = keys.get("statement");
                Key<String> textKey = keys.get("text");

                Key<String> idKey = trial.getKeys().get("trialAccount-id");

                List<Datum> statements = new ArrayList<>();

                KeySet retain = keys.with(idKey);

                Key<List<String>> tokenKey = null;
                Key<Spans<List<String>, String>> spansKey = null;

                for (Datum statement : trial.getSpannedData(sentenceKey, retain)) {

                    Datum tokenized = Tokenizer.tokenize(statement, textKey, retain);

                    KeySet tokenizedKeys = tokenized.getKeys();

                    tokenKey = tokenizedKeys.get(textKey + Tokenizer.SUFFIX);

                    spansKey = Key.of("placeName", RuntimeType.listSpans(String.class));

                    NER2Datum ner2Datum = new NER2Datum(
                            tokenKey,
                            ImmutableSet.of("placeName"),
                            spansKey,
                            true
                    );

                    String text = String.join(" ", tokenized.get(tokenKey));

                    String ner = StanfordNER.get(text);

//                    System.out.println(ner);
                    Datum nerd = ner2Datum.toDatum(ner);

                    if (tokenized.get(tokenKey).size() != nerd.get(tokenKey).size()) {

                        System.err.println("tokenised mismatch! Expected " + tokenized.get(tokenKey).size() + " got " + nerd.get(tokenKey).size());
                    } else {

                        tokenized = tokenized.with(nerd.getKeys().get("placeName"), nerd.get(spansKey));

                        statements.add(tokenized);

                        keys = keys
                                .with(tokenizedKeys)
                                .with(spansKey);
                    }
                }

                if (!statements.isEmpty()) {

                    System.out.println(id);

                    int i = 0;

                    ListIterator<Datum> jtr = statements.listIterator();
                    while (jtr.hasNext()) {
                        Datum statement = jtr.next();
                        Spans<List<String>, String> spans = statement.get(spansKey);

                        Spans<List<String>, Map> matchSpans = Spans.annotate(tokenKey, Map.class);

                        int j = 0;

                        for (Span<List<String>, String> span : spans) {

                            String candidate = String.join(" ", span.getSpanned(statement));

//                            System.out.println(candidate);

                            List<Match> matches = lookup.getMatches(candidate);

                            if (!matches.isEmpty()) {

                                String spanId = id + "-" + i + "-" + j;

                                Match match = matches.get(0);

                                Map<String, String> metadata = match.getMetadata();
                                String spanned = String.join(" ", span.getSpanned(statement));

                                metadata.put("trialId", id);
                                metadata.put("id", spanId);
                                metadata.put("spanned", spanned);
                                metadata.put("text", match.getText());
                                metadata.put("date", date.format(date2JS));

                                Span<List<String>, Map> matchSpan = Span.annotate(tokenKey, span.from(), span.to(), metadata);

                                this.matches.add(metadata);

                                matchSpans = matchSpans.with(matchSpan);
                            }
                            ++j;
                        }

                        statement = statement.with(placeMatchKey, matchSpans);

                        jtr.set(statement);

                        ++i;
                    }

                    Datum2SimpleDocument<?> datum2SimpleDocument = new Datum2SimpleDocument(tokenKey, ImmutableList.of(spansKey, placeMatchKey));

                    SimpleDocument document = datum2SimpleDocument.toDocument(id, statements);


                    if (!trialsByDate.containsKey(date)) {
                        trialsByDate.put(date, new ArrayList<>());
                    }

                    List<SimpleDocument> trialsForDate = trialsByDate.get(date);
                    trialsForDate.add(document);
                    trialsByDate.put(date, trialsForDate);
                    if (trialsById.containsKey(id)) {
                        System.err.println(id + " already exists");
                    }
                    trialsById.put(id, document);
                }
//                });
//            });
            }

        } catch (Throwable err) {
            err.printStackTrace ();
        }
    }

    public List<Map<LocalDate, List<SimpleDocument>>> getDocumentsByDate(LocalDate from, LocalDate to) {
        List<Map<LocalDate, List<SimpleDocument>>> trials = new ArrayList<>();
        for (LocalDate date = from; date.isBefore(to); date = date.plusDays(1))
        {
            trials.add(ImmutableMap.of(date, trialsByDate.get(date)));
        }
        return trials;
    }

    public Map<String, SimpleDocument> getDocumentsById() {
        return trialsById;
    }

    public List<Map<String, String>> getMatches() {
        return matches;
    }

    public KeySet keys() {
        return keys;
    }

    private LocalDate getDate(String id) {
        LocalDate date = LocalDate.parse(id.split("-")[0], id2Date);
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
