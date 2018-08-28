package uk.ac.susx.shl.micromacro.core.data.text;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import uk.ac.susx.shl.micromacro.client.StanfordNER;
import uk.ac.susx.shl.micromacro.core.data.Match;
import uk.ac.susx.shl.micromacro.core.data.geo.GeoJsonKnowledgeBase;
import uk.ac.susx.tag.method51.core.data.StoreException;
import uk.ac.susx.tag.method51.core.data.impl.PostgreSQLDatumStore;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.span.Span;
import uk.ac.susx.tag.method51.core.meta.span.Spans;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import com.joestelmach.natty.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final Map<LocalDate, List<Map<String, String>>> matchesByDate;

    private final GeoJsonKnowledgeBase lookup;
    private final PubMatcher pubMatcher;

    private final Key<Spans<List<String>, Map>> placeMatchKey;
    private final Key<Spans<List<String>, Map>> pubMatchKey;

    private final Path start;

    private final boolean allowSessionDates;

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

    private final Parser dateParser = new Parser();

    private final StanfordNER placeNerService;
    private final StanfordNER pubNerService;

    private final DB db;

    public OBTrials(String sessionsPath, String geoJsonPath, String obMapPath, StanfordNER placeNer, StanfordNER pubNer) throws IOException {
        lookup = new GeoJsonKnowledgeBase(Paths.get(geoJsonPath));
        placeMatchKey = Key.of("placeNameMatch", RuntimeType.listSpans(Map.class));
        pubMatchKey = Key.of("pubMatch", RuntimeType.listSpans(Map.class));
        keys = KeySet.of(placeMatchKey, pubMatchKey);
        start = Paths.get(sessionsPath);

        pubMatcher = new PubMatcher(false, false);

        this.placeNerService = placeNer;
        this.pubNerService = pubNer;

        allowSessionDates = false;

        db = DBMaker
                .fileDB(obMapPath)
                .fileMmapEnable()
                .closeOnJvmShutdown()
//                .readOnly()
                .make();

        trialsByDate =  (Map<LocalDate, List<SimpleDocument>>) db.hashMap("trials-by-date").createOrOpen();
        trialsById = (Map<String, SimpleDocument>) db.hashMap("trials-by-id").createOrOpen();
        matchesByDate = (Map<LocalDate, List<Map<String, String>>>) db.hashMap("matchesByDate").createOrOpen();
    }

    public void clear() {
        trialsByDate.clear();
        trialsById.clear();
        matchesByDate.clear();
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

        Key<String> statementIdKey = Key.of("statementId", RuntimeType.STRING);
        Key<String> sentenceIdKey = Key.of("sentenceId", RuntimeType.STRING);

        Key<Spans<String, String>> sessionsKey = Key.of("sessionsPaper", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> trialsKey = Key.of("trialAccount", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> statementsKey = Key.of("statement", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> crimeDateKey = Key.of("crimeDate", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> offenceCategoryKey = Key.of("offenceCategory", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> offenceSubcategoryKey = Key.of("offenceSubcategory", RuntimeType.stringSpans(String.class));
//        Key<Spans<String, String>> entities = Key.of("entities", RuntimeType.stringSpans(String.class));

        Map<Key<Spans<String, String>>, List<XML2Datum.Element>> interestingElements = new HashMap<>();


        interestingElements.put(sessionsKey, ImmutableList.of(
                new XML2Datum.Element("div0", ImmutableMap.of("type", "sessionsPaper"), "sessionsPaper").isContainer(true)
        ));

        interestingElements.put(trialsKey, ImmutableList.of(
                new XML2Datum.Element("div1", ImmutableMap.of("type", "trialAccount"), "trialAccount").valueAttribute("id")
        ));

        interestingElements.put(statementsKey, ImmutableList.of(
                new XML2Datum.Element("p", ImmutableMap.of(), "statement")
        ));

        interestingElements.put(offenceCategoryKey, ImmutableList.of(
                new XML2Datum.Element("interp", ImmutableMap.of("type", "offenceCategory"), "offenceCategory").valueAttribute("value")
        ));

        interestingElements.put(offenceSubcategoryKey, ImmutableList.of(
                new XML2Datum.Element("interp", ImmutableMap.of("type", "offenceSubcategory"), "offenceSubcategory").valueAttribute("value")
        ));

        interestingElements.put(crimeDateKey, ImmutableList.of(
//                new XML2Datum.Element("placeName", ImmutableMap.of(), "placeName"),
                new XML2Datum.Element("rs", ImmutableMap.of("type", "crimeDate"), "crimeDate"))
        );


        try {

            Set<Path> files = getFiles(from, to);

            Iterator<Datum> itr = XML2Datum.getData(start, files, interestingElements, "trialAccount", "-id").iterator();


            while(itr.hasNext()) {
                Datum trial = itr.next();

                String trialId = trial.get("trialAccount-id");

                if(trialsById.containsKey(trialId)) {
                    continue;
                }

                LocalDate sessionDate = getDate(trialId);
                Date refDate = Date.from(sessionDate.atTime(0,0,0).toInstant(ZoneOffset.UTC));

                KeySet keys = trial.getKeys();

                Key<String> textKey = keys.get("text");

                Key<String> trialIdKey = trial.getKeys().get("trialAccount-id");

                List<Datum> sentences = new ArrayList<>();

                Key<Spans<List<String>, String>> placeNameSpansKey = Key.of("placeName", RuntimeType.listSpans(String.class));
                Key<Spans<List<String>, String>> pubSpansKey = Key.of("pub", RuntimeType.listSpans(String.class));

                keys = keys
                        .with(trialIdKey)
                        .with(sentenceIdKey)
                        .with(statementsKey)
                        .with(placeNameSpansKey)
                        .with(pubSpansKey)
                ;

                KeySet retain = keys
//                        .with(crimeDate)
                        ;

                Key<List<String>> tokensKey = Key.of(textKey + Tokenizer.SUFFIX, RuntimeType.list(RuntimeType.STRING));

                int i = 0;
                for (Datum statement : trial.getSpannedData(statementsKey, retain)) {

                    String statementId = trialId + "-" + i++;

                    statement = statement.with(statementIdKey, statementId);

                    List<Datum> sents = Sentizer.sentize(statement, textKey, retain
                        .with(crimeDateKey)
                        .with(offenceCategoryKey)
                        .with(offenceSubcategoryKey)
                    );

                    int j = 0;
                    for (Datum sentence : sents) {

                        String sentenceId = statement.get(statementIdKey) + "-" + j++;
//                        System.out.println(sentenceId);

                        sentence = sentence.with(sentenceIdKey, sentenceId);

                        Datum tokenized = Tokenizer.tokenize(sentence, textKey, retain);

                        //retain original crime date / offcat spans - tokenisation not required
                        tokenized = tokenized.with(crimeDateKey, sentence.get(crimeDateKey));
                        tokenized = tokenized.with(offenceCategoryKey, sentence.get(offenceCategoryKey));
                        tokenized = tokenized.with(offenceSubcategoryKey, sentence.get(offenceSubcategoryKey));

                        KeySet tokenizedKeys = tokenized.getKeys();

                        keys = keys.with(tokenizedKeys);

                        NER2Datum ner2Datum = new NER2Datum(
                                tokensKey,
                                ImmutableSet.of("placeName"),
                                placeNameSpansKey,
                                true
                        );

                        String text = String.join(" ", tokenized.get(tokensKey));

                        //merge ner places
                        String placeNer = placeNerService.get(text);
    //                    System.out.println(placeNer);
                        Datum placeNerd = ner2Datum.toDatum(placeNer);

                        boolean error = false;
                        if (tokenized.get(tokensKey).size() != placeNerd.get(tokensKey).size()) {

                            System.err.println("tokenised mismatch! Expected " + tokenized.get(tokensKey).size() + " got " + placeNerd.get(tokensKey).size());
                            error = true;
                        } else {
                            tokenized = tokenized
                                    .with(placeNerd.getKeys().get("placeName"), placeNerd.get(placeNameSpansKey));
                        }

                        //merge ner pubs
                        String pubNer = pubNerService.get(text);
    //                    System.out.println(pubNer);
                        Datum pubNerd = ner2Datum.toDatum(pubNer, ImmutableSet.of("pub"), pubSpansKey);
                        if (tokenized.get(tokensKey).size() != pubNerd.get(tokensKey).size()) {

                            System.err.println("tokenised mismatch! Expected " + tokenized.get(tokensKey).size() + " got " + pubNerd.get(tokensKey).size());
                            error = true;
                        } else {
                            tokenized = tokenized
                                    .with(pubNerd.getKeys().get("pub"), pubNerd.get(pubSpansKey));
                        }

                        if (!error) {
                            sentences.add(tokenized);
                        }
                    }
                }

                if (!sentences.isEmpty()) {

                    System.out.println(trialId);

                    //figure out date first
                    Optional<LocalDate> crimeDate = Optional.empty();
                    Optional<String> offenceCategory = Optional.empty();
                    Optional<String> offenceSubcategory = Optional.empty();

                    for(Datum sentence : sentences) {
                        if(!crimeDate.isPresent()) {
                            crimeDate = getFirstDate(sentence, crimeDateKey, refDate);
                        }
                        if(!offenceCategory.isPresent()) {
                            offenceCategory = getCrimeCat(sentence, offenceCategoryKey);
                        }
                        if(!offenceSubcategory.isPresent()) {
                            offenceSubcategory = getCrimeCat(sentence, offenceSubcategoryKey);
                        }
                    }

                    LocalDate date;
                    boolean usingSessionDate = false;
                    if(crimeDate.isPresent()) {
                        date = crimeDate.get();
                    } else {
                        date = sessionDate;
                        usingSessionDate = true;
                    }


                    boolean addByDate = allowSessionDates || !allowSessionDates && !usingSessionDate;

                    ListIterator<Datum> jtr = sentences.listIterator();
                    while (jtr.hasNext()) {
                        Datum sentence = jtr.next();

                        String sentenceId = sentence.get(sentenceIdKey);

                        sentence = processPlaceNames(sentence, placeNameSpansKey, tokensKey, trialId, sentenceId, date, offenceCategory, offenceSubcategory);

                        sentence = processPubs(sentence, pubSpansKey, tokensKey, trialId, sentenceId, date, offenceCategory, offenceSubcategory);

                        if(addByDate) {

                            for(Span<List<String>, Map> match : sentence.get(placeMatchKey) ) {
                                Map metadata = match.get();
                                matchesByDate.computeIfAbsent(date, k -> new ArrayList<>());
                                List tmp = matchesByDate.get(date);
                                tmp.add(metadata);
                                matchesByDate.put(date, tmp);
                            }

                            for(Span<List<String>, Map> match : sentence.get(pubMatchKey) ){
                                Map metadata = match.get();
                                matchesByDate.computeIfAbsent(date, k -> new ArrayList<>());
                                List tmp = matchesByDate.get(date);
                                tmp.add(metadata);
                                matchesByDate.put(date, tmp);
                            }
                        }

                        jtr.set(sentence);

                    }

                    Datum2SimpleDocument<?> datum2SimpleDocument = new Datum2SimpleDocument(tokensKey, ImmutableList.of(
                            placeNameSpansKey,
                            placeMatchKey,
                            pubSpansKey,
                            pubMatchKey,
                            Key.of("crimeDate-token", RuntimeType.listSpans(String.class))
                    ));

                    SimpleDocument document = datum2SimpleDocument.toDocument(trialId, sentences);

                    if(offenceCategory.isPresent()) {
                        document = document.with("offCat", offenceCategory.get());
                    }

                    if(offenceSubcategory.isPresent()) {
                        document = document.with("offSubcat", offenceSubcategory.get());
                    }

                    if(addByDate) {
                        if (!trialsByDate.containsKey(date)) {
                            trialsByDate.put(date, new ArrayList<>());
                        }

                        List<SimpleDocument> trialsForDate = trialsByDate.get(date);
                        trialsForDate.add(document);
                        trialsByDate.put(date, trialsForDate);
                        if (trialsById.containsKey(trialId)) {
                            System.err.println(trialId + " already exists");
                        }
                    }
                    trialsById.put(trialId, document);
                }
//                });
//            });
            }
            db.commit();

        } catch (Throwable err) {
            err.printStackTrace ();
        }
    }


    public void saveSents2Table(LocalDate from, LocalDate to, PostgreSQLDatumStore.Builder storeBuilder) throws StoreException {


        Key<String> sentenceIdKey= Key.of("sentenceId", RuntimeType.STRING);
        Key<String> statementIdKey = Key.of("statementId", RuntimeType.STRING);
        Key<List<String>> placeNamesKey =  Key.of("placeNames", RuntimeType.list(RuntimeType.STRING));
        storeBuilder.uniqueIndex(statementIdKey);

        Key<Spans<List<String>, String>> placeNameSpansKey = Key.of("placeName", RuntimeType.listSpans(String.class));


        Key<Spans<String, String>> sessionsKey = Key.of("sessionsPaper", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> trialsKey = Key.of("trialAccount", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> statementsKey = Key.of("statement", RuntimeType.stringSpans(String.class));
//        Key<Spans<String, String>> crimeDateKey = Key.of("crimeDate", RuntimeType.stringSpans(String.class));
//        Key<Spans<String, String>> entities = Key.of("entities", RuntimeType.stringSpans(String.class));

        Map<Key<Spans<String, String>>, List<XML2Datum.Element>> interestingElements = new HashMap<>();


        interestingElements.put(sessionsKey, ImmutableList.of(
                new XML2Datum.Element("div0", ImmutableMap.of("type", "sessionsPaper"), "sessionsPaper").isContainer(true)
        ));

        interestingElements.put(trialsKey, ImmutableList.of(
                new XML2Datum.Element("div1", ImmutableMap.of("type", "trialAccount"), "trialAccount").valueAttribute("id")
        ));

        interestingElements.put(statementsKey, ImmutableList.of(
                new XML2Datum.Element("p", ImmutableMap.of(), "statement")
        ));

//        interestingElements.put(crimeDateKey, ImmutableList.of(
////                new XML2Datum.Element("placeName", ImmutableMap.of(), "placeName"),
//                new XML2Datum.Element("rs", ImmutableMap.of("type", "crimeDate"), "crimeDate"))
//        );


        PostgreSQLDatumStore store = storeBuilder
                .uniqueIndex(sentenceIdKey)
                .build();
        store.connect();
        boolean keysEnsured = false;

        try {

            Set<Path> files = getFiles(from, to);

            Iterator<Datum> itr = XML2Datum.getData(start, files, interestingElements, "trialAccount", "-id").iterator();

//            ForkJoinPool forkJoinPool = new ForkJoinPool(4);
//
//            forkJoinPool.submit(() -> {
//                Iterable<Datum> iterable = () -> itr;
            int idx = 0;
            while(itr.hasNext()) {
                Datum trial = itr.next();

                String trialId = trial.get("trialAccount-id");

                LocalDate sessionDate = getDate(trialId);
                Date refDate = Date.from(sessionDate.atTime(0,0,0).toInstant(ZoneOffset.UTC));
//                if(trialsByDate.containsKey(date)) {
//                    continue;
//                }

//                Stream<Datum> stream = StreamSupport.stream(iterable.spliterator(),true);

//                stream.forEach(trial -> {

                KeySet keys = trial.getKeys();
                Key<String> textKey = keys.get("text");

                Key<String> trialIdKey = trial.getKeys().get("trialAccount-id");
                List<Datum> sentences = new ArrayList<>();

                KeySet retain = keys
                        .with(trialIdKey)
                        .with(statementIdKey)
                        .with(sentenceIdKey)
                        .with(placeNameSpansKey)
//                        .with(crimeDate)
                        ;

                Key<List<String>> tokenKey = null;

                int i = 0;
                for (Datum statement : trial.getSpannedData(statementsKey, retain)) {

                    statement = statement.with(statementIdKey, trialId+"-"+i);

                    List<Datum> sents = Sentizer.sentize(statement, textKey, retain);
                    int j = 0;
                    for(Datum sentence : sents) {

                        sentence = sentence.with(sentenceIdKey, statement.get(statementIdKey)+"-"+j);

                        Datum tokenized = Tokenizer.tokenize(sentence, textKey, retain);

                        KeySet tokenizedKeys = tokenized.getKeys();

                        tokenKey = tokenizedKeys.get(textKey + Tokenizer.SUFFIX);



                        NER2Datum ner2Datum = new NER2Datum(
                                tokenKey,
                                ImmutableSet.of("placeName"),
                                placeNameSpansKey,
                                true
                        );

                        String text = String.join(" ", tokenized.get(tokenKey));

                        String ner = placeNerService.get(text);

//                    System.out.println(ner);
                        Datum nerd = ner2Datum.toDatum(ner);

                        //retain original crime date spans - tokenisation not required
//                        tokenized = tokenized.with(crimeDateKey, sentence.get(crimeDateKey));

                        if (tokenized.get(tokenKey).size() != nerd.get(tokenKey).size()) {

                            System.err.println("tokenised mismatch! Expected " + tokenized.get(tokenKey).size() + " got " + nerd.get(tokenKey).size());
                        } else {

                            tokenized = tokenized
                                    .with(nerd.getKeys().get("placeName"), nerd.get(placeNameSpansKey))
                            ;

                            sentences.add(tokenized);

                            keys = keys
                                    .with(tokenizedKeys)
                                    .with(placeNameSpansKey);
                        }

                        ++j;
                    }
                    ++i;
                }

                KeySet storeKeys = KeySet.of(
                    trialIdKey,
                    statementIdKey,
                    sentenceIdKey,
                    textKey,
//                    placeNameSpansKey,
                    placeNamesKey
                );

                if(!keysEnsured) {
                    store.addKeys(storeKeys);
                    keysEnsured = true;
                }

                if (!sentences.isEmpty()) {

                    System.out.println(trialId);


                    for(Datum sentence : sentences) {


                        Datum datum = new Datum();

                        datum = datum
                                .with(trialIdKey, trialId)
                                .with(statementIdKey, sentence.get(statementIdKey))
                                .with(sentenceIdKey, sentence.get(sentenceIdKey))
                                .with(textKey, sentence.get(textKey));
                        Spans<List<String>, String> placeNameSpans = sentence.get(placeNameSpansKey);
                        if(!placeNameSpans.get().isEmpty()){

//                            datum = datum.with(placeNameSpansKey, placeNameSpans);

                            List<String> placeNames = new ArrayList<>();
                            for(Span<List<String>, String> span : placeNameSpans.get()) {
                                placeNames.add(String.join(" ", span.getSpanned(sentence)));
                            }

                            datum = datum.with(placeNamesKey, placeNames);
                        }


                        store.set(datum);
                    }

                    store.commit();
                }
//                });
//            });
            }

        } catch (Throwable err) {
            err.printStackTrace ();
        }
    }

    public void saveStatements2Table(LocalDate from, LocalDate to, PostgreSQLDatumStore.Builder storeBuilder) throws StoreException {


        Key<String> statementIdKey = Key.of("statementId", RuntimeType.STRING);
        Key<List<String>> placeNamesKey =  Key.of("placeNames", RuntimeType.list(RuntimeType.STRING));
        storeBuilder.uniqueIndex(statementIdKey);

        Key<Spans<List<String>, String>> placeNameSpansKey = Key.of("placeName", RuntimeType.listSpans(String.class));


        Key<Spans<String, String>> sessionsKey = Key.of("sessionsPaper", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> trialsKey = Key.of("trialAccount", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> statementsKey = Key.of("statement", RuntimeType.stringSpans(String.class));
//        Key<Spans<String, String>> crimeDateKey = Key.of("crimeDate", RuntimeType.stringSpans(String.class));
//        Key<Spans<String, String>> entities = Key.of("entities", RuntimeType.stringSpans(String.class));

        Map<Key<Spans<String, String>>, List<XML2Datum.Element>> interestingElements = new HashMap<>();


        interestingElements.put(sessionsKey, ImmutableList.of(
                new XML2Datum.Element("div0", ImmutableMap.of("type", "sessionsPaper"), "sessionsPaper").isContainer(true)
        ));

        interestingElements.put(trialsKey, ImmutableList.of(
                new XML2Datum.Element("div1", ImmutableMap.of("type", "trialAccount"), "trialAccount").valueAttribute("id")
        ));

        interestingElements.put(statementsKey, ImmutableList.of(
                new XML2Datum.Element("p", ImmutableMap.of(), "statement")
        ));

//        interestingElements.put(crimeDateKey, ImmutableList.of(
////                new XML2Datum.Element("placeName", ImmutableMap.of(), "placeName"),
//                new XML2Datum.Element("rs", ImmutableMap.of("type", "crimeDate"), "crimeDate"))
//        );


        PostgreSQLDatumStore store = storeBuilder
                .uniqueIndex(statementIdKey)
                .build();
        store.connect();
        boolean keysEnsured = false;

        try {

            Set<Path> files = getFiles(from, to);

            Iterator<Datum> itr = XML2Datum.getData(start, files, interestingElements, "trialAccount", "-id").iterator();

//            ForkJoinPool forkJoinPool = new ForkJoinPool(4);
//
//            forkJoinPool.submit(() -> {
//                Iterable<Datum> iterable = () -> itr;
            int idx = 0;
            while(itr.hasNext()) {
                Datum trial = itr.next();

                String trialId = trial.get("trialAccount-id");

                LocalDate sessionDate = getDate(trialId);
                Date refDate = Date.from(sessionDate.atTime(0,0,0).toInstant(ZoneOffset.UTC));
//                if(trialsByDate.containsKey(date)) {
//                    continue;
//                }

//                Stream<Datum> stream = StreamSupport.stream(iterable.spliterator(),true);

//                stream.forEach(trial -> {

                KeySet keys = trial.getKeys();
                Key<String> textKey = keys.get("text");

                Key<String> trialIdKey = trial.getKeys().get("trialAccount-id");
                List<Datum> statements = new ArrayList<>();

                KeySet retain = keys
                        .with(trialIdKey)
                        .with(statementIdKey)
                        .with(placeNameSpansKey)
//                        .with(crimeDate)
                        ;

                Key<List<String>> tokenKey = null;

                int i = 0;
                for (Datum statement : trial.getSpannedData(statementsKey, retain)) {

                    statement = statement.with(statementIdKey, trialId+"-"+i);

                    Datum tokenized = Tokenizer.tokenize(statement, textKey, retain);

                    KeySet tokenizedKeys = tokenized.getKeys();

                    tokenKey = tokenizedKeys.get(textKey + Tokenizer.SUFFIX);

                    NER2Datum ner2Datum = new NER2Datum(
                            tokenKey,
                            ImmutableSet.of("placeName"),
                            placeNameSpansKey,
                            true
                    );

                    String text = String.join(" ", tokenized.get(tokenKey));

                    String ner = placeNerService.get(text);

//                    System.out.println(ner);
                    Datum nerd = ner2Datum.toDatum(ner);

                    //retain original crime date spans - tokenisation not required
//                        tokenized = tokenized.with(crimeDateKey, sentence.get(crimeDateKey));

                    if (tokenized.get(tokenKey).size() != nerd.get(tokenKey).size()) {

                        System.err.println("tokenised mismatch! Expected " + tokenized.get(tokenKey).size() + " got " + nerd.get(tokenKey).size());
                    } else {

                        tokenized = tokenized
                                .with(nerd.getKeys().get("placeName"), nerd.get(placeNameSpansKey))
                        ;

                        statements.add(tokenized);

                        keys = keys
                                .with(tokenizedKeys)
                                .with(placeNameSpansKey);
                    }

                    ++i;
                }


                KeySet storeKeys = KeySet.of(
                        trialIdKey,
                        statementIdKey,
                        textKey,
//                    placeNameSpansKey,
                        placeNamesKey
                );

                if(!keysEnsured) {
                    store.addKeys(storeKeys);
                    keysEnsured = true;
                }

                if (!statements.isEmpty()) {

                    System.out.println(trialId);


                    for(Datum statement : statements) {


                        Datum datum = new Datum();

                        datum = datum
                                .with(trialIdKey, trialId)
                                .with(statementIdKey, statement.get(statementIdKey))
                                .with(textKey, statement.get(textKey));
                        Spans<List<String>, String> placeNameSpans = statement.get(placeNameSpansKey);
                        if(!placeNameSpans.get().isEmpty()){

//                            datum = datum.with(placeNameSpansKey, placeNameSpans);

                            List<String> placeNames = new ArrayList<>();
                            for(Span<List<String>, String> span : placeNameSpans.get()) {
                                placeNames.add(String.join(" ", span.getSpanned(statement)));
                            }

                            datum = datum.with(placeNamesKey, placeNames);
                        }


                        store.set(datum);
                    }

                    store.commit();
                }
//                });
//            });
            }

        } catch (Throwable err) {
            err.printStackTrace ();
        }
    }


    private Optional<String> getCrimeCat(Datum datum, Key<Spans<String, String>> datesKey ) {

        Optional<String> crime = Optional.empty();

        Spans<String, String> crimeCatSpans = datum.get(datesKey);

        if(crimeCatSpans.get().size() == 1) {
            Span<String, String> span = crimeCatSpans.get(0);

            String crimeText = span.get();
            System.out.println(crimeText);

            crime = Optional.of(crimeText);
        }

        return crime;
    }

    private final Pattern dateFix = Pattern.compile("(\\d{1,2})d" );

    private String fixDate(String broken ) {

        Matcher m = dateFix.matcher(broken);

        String fixed = broken;

        if(m.find()) {
            fixed = m.replaceAll("$1");
        }

        return fixed;
    }

    private Optional<LocalDate> getFirstDate(Datum datum, Key<Spans<String, String>> datesKey, Date refDate) {

        LocalDate _refDate = refDate.toInstant().atOffset(ZoneOffset.UTC).toLocalDate();

        Spans<String, String> dateSpans = datum.get(datesKey);

        Optional<LocalDate> date = Optional.empty();

        if(dateSpans.get().size() >= 1) {
            Span<String, String> span = dateSpans.get(0);

            String dateText = fixDate(span.getSpanned(datum));

            System.out.println(dateText);

            List<DateGroup> dateGroups = dateParser.parse(dateText, refDate);

            if(dateGroups.size() == 1) {

                List<Date> parsed = dateGroups.get(0).getDates();

                if(parsed.size() == 1) {

                    LocalDate d = parsed.get(0).toInstant().atOffset(ZoneOffset.UTC).toLocalDate();

                    if(d.isAfter(_refDate)) {
                        d = d.minusYears(1);
                    }

                    date = Optional.of(d);
                }
            }
        }

        return date;
    }

    private Datum processPubs(Datum datum, Key<Spans<List<String>, String>> pubSpanKey,
                                Key<List<String>> tokenKey, String trialId, String sentenceId, LocalDate date,
                                Optional<String> offenceCategory, Optional<String> offenceSubcategory) {
        Spans<List<String>, String> pubSpans = datum.get(pubSpanKey);

        Spans<List<String>, Map> pubMatchSpans = Spans.annotate(tokenKey, Map.class);

        int j = 0;

        for (Span<List<String>, String> span : pubSpans) {

            String candidate = String.join(" ", span.getSpanned(datum));

//          System.out.println(candidate);

            List<PubMatcher.Pub> matches = pubMatcher.getPubs(candidate);

            if (!(matches == null || matches.isEmpty() || matches.get(0).match == null)) {

                Match match = matches.get(0).match;

                Map<String, String> metadata = match.getMetadata();
                String spanned = String.join(" ", span.getSpanned(datum));

                metadata.put("trialId", trialId);
                metadata.put("sentenceId", sentenceId);
                metadata.put("spanIdx", Integer.toString(j));
                metadata.put("spanned", spanned);
                metadata.put("text", match.getText());
                metadata.put("date", date.format(date2JS));
                metadata.put("type", "pub");
                metadata.put("offCat", offenceCategory.isPresent() ? offenceCategory.get() : null);
                metadata.put("offSubCat", offenceSubcategory.isPresent() ? offenceSubcategory.get() : null);

                Span<List<String>, Map> placeNameMatchSpan = Span.annotate(tokenKey, span.from(), span.to(), metadata);

//                matchesByDate.computeIfAbsent(date, k -> new ArrayList<>());
//                List tmp = matchesByDate.get(date);
//                tmp.add(metadata);
//                matchesByDate.put(date, tmp);
                pubMatchSpans = pubMatchSpans.with(placeNameMatchSpan);
            }
            ++j;
        }

        datum = datum.with(pubMatchKey, pubMatchSpans);
        return datum;
    }

    private Datum processPlaceNames(Datum datum, Key<Spans<List<String>, String>> placeNameSpansKey,
                                    Key<List<String>> tokenKey, String trialId, String sentenceId, LocalDate date,
                                    Optional<String> offenceCategory, Optional<String> offenceSubcategory) {
        Spans<List<String>, String> placeNameSpans = datum.get(placeNameSpansKey);

        Spans<List<String>, Map> placeNameMatchSpans = Spans.annotate(tokenKey, Map.class);

        int j = 0;

        for (Span<List<String>, String> span : placeNameSpans) {

            String candidate = String.join(" ", span.getSpanned(datum));

//          System.out.println(candidate);

            List<Match> matches = lookup.getMatches(candidate);

            if (!matches.isEmpty()) {

                Match match = matches.get(0);

                Map<String, String> metadata = match.getMetadata();
                String spanned = String.join(" ", span.getSpanned(datum));

                metadata.put("trialId", trialId);
                metadata.put("sentenceId", sentenceId);
                metadata.put("spanIdx", Integer.toString(j));
                metadata.put("spanned", spanned);
                metadata.put("text", match.getText());
                metadata.put("date", date.format(date2JS));
                metadata.put("type", "place");
                metadata.put("offCat", offenceCategory.isPresent() ? offenceCategory.get() : null);
                metadata.put("offSubCat", offenceSubcategory.isPresent() ? offenceSubcategory.get() : null);

                Span<List<String>, Map> placeNameMatchSpan = Span.annotate(tokenKey, span.from(), span.to(), metadata);

//                matchesByDate.computeIfAbsent(date, k -> new ArrayList<>());
//                List tmp = matchesByDate.get(date);
//                tmp.add(metadata);
//                matchesByDate.put(date, tmp);

                placeNameMatchSpans = placeNameMatchSpans.with(placeNameMatchSpan);
            }
            ++j;
        }

        datum = datum.with(placeMatchKey, placeNameMatchSpans);
        return datum;
    }

    public Map<LocalDate, List<SimpleDocument>> getDocumentsByDate(LocalDate from, LocalDate to) {
        Map<LocalDate, List<SimpleDocument>> trials = new HashMap<>();
        for (LocalDate date = from; date.isBefore(to); date = date.plusDays(1))
        {
            if(trialsByDate.containsKey(date)) {
                trials.computeIfAbsent(date, k->new ArrayList<>()).addAll(trialsByDate.get(date));
            }
        }
        return trials;
    }

    public Map<String, SimpleDocument> getDocumentsById() {
        return trialsById;
    }

    public Map<LocalDate, List<Map<String, String>>> getMatches(LocalDate from, LocalDate to) {
        Map<LocalDate, List<Map<String, String>>> matches = new HashMap<>();
        for (LocalDate date = from; date.isBefore(to); date = date.plusDays(1))
        {
            if (matchesByDate.containsKey(date)) {
                matches.computeIfAbsent(date, k -> new ArrayList<>()).addAll(matchesByDate.get(date));
            }
        }
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
//        LocalDate date = LocalDate.parse("t18120219-65".split("-")[0], new DateTimeFormatterBuilder()
//                .appendLiteral('t')
//                .appendValue(YEAR, 4)
//                .appendValue(MONTH_OF_YEAR, 2)
//                .appendValue(DAY_OF_MONTH, 2)
//                .toFormatter());
//        System.out.println(date.toString());



    }
}
