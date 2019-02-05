package uk.ac.susx.shl.micromacro.core.data.text;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jdbi.v3.core.Jdbi;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.postgresql.ds.PGSimpleDataSource;
import uk.ac.susx.shl.micromacro.jdbi.Method52DAO;
import uk.ac.susx.shl.micromacro.core.data.Match;
import uk.ac.susx.shl.micromacro.core.data.geo.GeoJsonKnowledgeBase;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.span.Span;
import uk.ac.susx.tag.method51.core.meta.span.Spans;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PubMatcher {
    private static final Logger LOG = Logger.getLogger(PubMatcher.class.getName());



    public static class Pub implements Serializable {
        public final String id;
        public final String name;
        public final List<String> addr;
        public final String parish;

        public final Match match;

        public Pub(String id, String name, String parish, String addr1, String addr2, String addr3, String addr4, String addr5, String addr6) {
            this.id = id;
            this.name = name;
            this.parish = parish;
            addr = ImmutableList.of(addr1, addr2, addr3, addr4, addr5, addr6);
            match = null;
        }

        private Pub(String id, String name, String parish, List<String> addr, Match match) {
            this.id = id;
            this.name = name;
            this.parish = parish;
            this.addr = ImmutableList.copyOf(addr);
            this.match = match;
        }

        public Pub match(Match match) {
            return new Pub(id, name, parish, addr, match);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pub pub = (Pub) o;
            return Objects.equals(name, pub.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }


    private final GeoJsonKnowledgeBase lookup;

    private Map<String, List<Pub>> pubs;

    private Map<String, List<Pub>> pubHash;

    private final DB db;

    private final boolean lc;


    public PubMatcher (boolean clear, boolean lc) throws IOException {
        db = DBMaker
                .fileDB("data/pubDB3")
                .fileMmapEnable()
                .closeOnJvmShutdown()
//                .readOnly()
                .make();


        lookup = new GeoJsonKnowledgeBase(Paths.get("data/LL_PL_PA_WA_POINTS_FeaturesT.json"));

        pubs = (Map)db.hashMap("pubs-by-name").createOrOpen();
        pubHash = (Map)db.hashMap("pubs-by-1st-token").createOrOpen();

//        pubs = new HashMap<>();
//        pubHash = new HashMap<>();

        this.lc = lc;

        if(clear) {
            pubs.clear();
            pubs.putAll(csv2Pubs("data/oldbailey-alex_1800-pubs-2.csv"));

            pubHash.clear();
            pubHash.putAll(pub2Hash(pubs));

            db.commit();
        }
    }

    private Map<String, List<Pub>> csv2Pubs(String path) throws IOException {

        Iterable<CSVRecord> records = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(new FileReader(path));

        Map<String, List<Pub>> pubs = new HashMap<>();

        int i = 0;

        for(CSVRecord record : records) {

            String name = record.get("pub_name");
            name = clean(name);
            String id = Integer.toString(i);

            String parish = clean(record.get("parish"));
            String add1 = clean(record.get("pub_add_1"));
            String add2 = clean(record.get("pub_add_2"));
            String add3 = clean(record.get("pub_add_3"));
            String add4 = clean(record.get("pub_add_4"));
            String add5 = clean(record.get("pub_add_5"));
            String add6 = clean(record.get("pub_add_6"));

            Pub pub = new Pub(
                id,
                name,
                parish,
                add1,
                add2,
                add3,
                add4,
                add5,
                add6
            );
            pub = lookupPub(pub);

            pubs.computeIfAbsent(name, k-> new ArrayList<>()).add(pub);

            Matcher m = andPattern.matcher(name);
            if(m.find()) {
                String andName = m.replaceAll("&");
                Pub andPub = new Pub(
                        id,
                        andName,
                        parish,
                        add1,
                        add2,
                        add3,
                        add4,
                        add5,
                        add6
                );

                andPub = lookupPub(andPub);

                pubs.computeIfAbsent(andName, k-> new ArrayList<>()).add(andPub);
            }

            m = ampersandPattern.matcher(name);
            if(m.find()) {
                String andName = m.replaceAll("and");
                Pub andPub = new Pub(
                        id,
                        andName,
                        parish,
                        add1,
                        add2,
                        add3,
                        add4,
                        add5,
                        add6
                );

                andPub = lookupPub(andPub);
                pubs.computeIfAbsent(andName, k-> new ArrayList<>()).add(andPub);
            }

            ++i;
        }

        return pubs;
    }

    private Map<String, List<Pub>> pub2Hash(Map<String, List<Pub>> pubs) {

        Map<String, List<Pub>> pubHash = new HashMap<>();

        for(Pub pub : pubs.values().stream().flatMap(Collection::stream).collect(Collectors.toList())) {

            String name = pub.name;

            if(lc) {
                name = name.toLowerCase();
            }

            String first = name.split(" ")[0];

            if(!pubHash.containsKey(first)) {
                pubHash.put(first, new ArrayList<>());
            }

            List<Pub> tmp = pubHash.get(first);
            tmp.add(pub);

            pubHash.put(first, tmp);
        }

        return pubHash;
    }

    private Pattern leadingNumbers = Pattern.compile("^[\\d\\s\\p{Punct}a]+(.*)");
    private Pattern andPattern = Pattern.compile("\\b[aA]nd\\b");
    private Pattern ampersandPattern = Pattern.compile("\\b&\\b");
    private Pattern whitespace = Pattern.compile("req");

    private String clean(String original) {

        String trimmed = whitespace.matcher(original).replaceAll(" ").trim();

        trimmed = leadingNumbers.matcher(original)
                .replaceFirst("$1")
                .replaceAll("[()\\]\\[]", "");


        return trimmed;
    }


    public List<Pub> getMatchedPubs() {

        List<Pub> matchedPubs = pubs.values().stream().flatMap(Collection::stream).filter(pub->pub.match != null).collect(Collectors.toList());

        return matchedPubs;
    }

    public List<Pub> getUnmatchedPubs() {

        List<Pub> matchedPubs = pubs.values().stream().flatMap(Collection::stream).filter(pub->pub.match == null).collect(Collectors.toList());

        return matchedPubs;
    }

    public List<Pub> getPubs() {

        List<Pub> matchedPubs = pubs.values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        return matchedPubs;    }

    public List<Pub> getPubs(String candidate) {

        return pubs.get(candidate);
    }



    private Pub lookupPub(Pub pub) {

        Match match = null;

        String pubName = pub.name;

        LOG.info(pubName);

        List<Match> candidates = lookup.getMatches(pubName);

        if(candidates.isEmpty()) {

            String addr = clean(pub.addr.get(0));

            LOG.info(addr);

            candidates = lookup.getMatches(addr);
        }
//
//        if(candidates.isEmpty()) {
//
//            String addr = clean(pub.addr.get(1));
//
//            LOG.info(addr);
//
//            candidates = lookup.getMatches(addr);
//        }

        if(!candidates.isEmpty()) {
            LOG.info("---MATCH---");
            match = candidates.get(0);
        }

        return pub.match(match);
    }


    public Spans<List<String>, String> matchPubs(Datum datum, Key<List<String>> tokenKey) {

        Spans<List<String>, String> pubSpans = Spans.annotate(tokenKey, String.class);

        List<String> tokens = datum.get(tokenKey);

        String raster = String.join(" ", tokens);
        if(lc) {
            raster = raster.toLowerCase();
        }

        Map<Integer, Integer> indexMap = new HashMap<>();

        int k = 0;
        for(int i = 0; i < tokens.size(); ++i) {
            String token = tokens.get(i);

            for(int j = 0; j < token.length(); ++j, ++k) {

                indexMap.put(k+i, i);
            }
        }

        int i = 0;
        int j = 0;

        boolean pubSearch = false;

        for(String token : tokens) {

            if(pubSearch) {

                if(lc) {
                    token = token.toLowerCase();
                }
                if(pubHash.containsKey(token)) {

                    List<Pub> pubs = pubHash.get(token);

//                System.out.println(pubs.size());

                    List<Pub> candidates = new ArrayList<>();

                    int maxLength = 0;
                    for(Pub pub : pubs) {

                        String pubName = pub.name;

                        if(lc) {
                            pubName = pubName.toLowerCase();
                        }

                        int max = raster.length()-1;
                        int from = Math.min(j+i, max);
                        int to = Math.min(j+i+pubName.length(), max);

                        if(raster.substring(from, to).equals(pubName)) {
                            if(pub.name.length() > maxLength) {
                                maxLength = pub.name.length();
                            }
//                            System.out.println(pub.name);
//                            System.out.println(i);
                            candidates.add(pub);
                        }
                    }

                    ListIterator<Pub> itr = candidates.listIterator();
                    while(itr.hasNext()) {
                        Pub candidate =  itr.next();
                        if(candidate.name.length() < maxLength) {
                            itr.remove();
                        }
                    }

                    if(candidates.size() >= 1) {

                        Pub p = candidates.get(0);

                        int f = indexMap.get(j+i);

                        int t = indexMap.get(j+i+p.name.length()-1)+1;

                        t = Math.min(t, tokens.size());

                        String match = String.join(" ", tokens.subList(f,t));

                        pubSpans = pubSpans.with(f, t, "pub");
                    }


                }
            }

            j += token.length();
            ++i;

            pubSearch = token.equalsIgnoreCase("the");
        }

        return pubSpans;
    }

    public static void rebuildIndex() throws  Exception {
        PubMatcher pm = new PubMatcher(true, false);
    }

    public static void getUnmatched() throws Exception {

        PubMatcher pm = new PubMatcher(false, false);

        try (
            BufferedWriter writer = Files.newBufferedWriter(Paths.get("unmatchedPubs.csv"))
        ) {
            writer.write("id,");
            writer.write("name,");
            writer.write("parish,");
            writer.write("pub_add_1,");
            writer.write("pub_add_2,");
            writer.write("pub_add_3,");
            writer.write("pub_add_4,");
            writer.write("pub_add_5,");
            writer.write("pub_add_6");
            writer.newLine();
            for(Pub pub : pm.getUnmatchedPubs()) {
                writer.write(pub.id);
                writer.write( ",");
                writer.write(pub.name);
                writer.write( ",");
                writer.write(pub.parish);
                writer.write( ",");
                writer.write(pub.addr.get(0));
                writer.write( ",");
                writer.write(pub.addr.get(1));
                writer.write( ",");
                writer.write(pub.addr.get(2));
                writer.write( ",");
                writer.write(pub.addr.get(3));
                writer.write( ",");
                writer.write(pub.addr.get(4));
                writer.write( ",");
                writer.write(pub.addr.get(5));
                writer.newLine();
            }
        }
    }


    public static void getMatchedOverTime() throws Exception {

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://127.0.0.1/oldbailey-alex");

        String table = "1750-1825-sent-placeNames-2";
        String trialIdKey = "general/trialAccount-id";
        String _sentenceIdKey = "general/sentenceId";
        List<String> annotationKeys = ImmutableList.of("classify/1750-1825-places2-publocation");


        Jdbi jdbi = Jdbi.create(dataSource);

        Method52DAO method52Data = new Method52DAO(jdbi);

        PubMatcher pm = new PubMatcher(false, false);

        Path start = Paths.get("data/sessionsPapers");

        Set<Path> paths = new OBFiles(start)
            .getFiles(LocalDate.of(1800,1,1), LocalDate.of(1801,12,31));
//            .getFiles(LocalDate.of(1686,1,1), LocalDate.of(1914,12,31));

        Map<Key<Spans<String, String>>, List<XML2Datum.Element>> interestingElements = new HashMap<>();

        Key<Spans<String, String>> sessionsKey = Key.of("sessionsPaper", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> trialsKey = Key.of("trialAccount", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> statementsKey = Key.of("statement", RuntimeType.stringSpans(String.class));
        Key<String> sentenceIdKey = Key.of("sentenceId", RuntimeType.STRING);


        interestingElements.put(sessionsKey, ImmutableList.of(
                new XML2Datum.Element("div0", ImmutableMap.of("type", "sessionsPaper"), "sessionsPaper").isContainer(true)
        ));

        interestingElements.put(trialsKey, ImmutableList.of(
                new XML2Datum.Element("div1", ImmutableMap.of("type", "trialAccount"), "trialAccount").valueAttribute("id")
        ));

        interestingElements.put(statementsKey, ImmutableList.of(
                new XML2Datum.Element("p", ImmutableMap.of(), "statement")
        ));

        Iterator<Datum> itr = XML2Datum.getData(start, paths, interestingElements, "trialAccount", "-id").iterator();

        Key<Spans<List<String>, String>> pubSpansKey = Key.of("pubs", RuntimeType.listSpans(String.class));


        Map<String, Map<String, Integer>> matchedPubs = new HashMap<>();

        List<Pub> pubs = pm.getPubs();

        while(itr.hasNext()) {
            Datum trial = itr.next();

            String trialId = trial.get("trialAccount-id");

            List<Datum> classified = method52Data.getScores(table, trialIdKey, _sentenceIdKey, annotationKeys, ImmutableList.of(trialId));

            String year = trialId.substring(1, 5);

            System.out.println(year);

            KeySet keys = trial.getKeys();

            Key<String> textKey = keys.get("text");

            for (Datum statement : trial.getSpannedData(statementsKey, keys)) {

                List<Datum> sents = Sentizer.sentize(statement, textKey, keys);

                for(Datum sentence : sents ) {

                    Datum tokenized = Tokenizer.tokenize(sentence, textKey, keys);
                    KeySet tokenizedKeys = tokenized.getKeys();
                    Key<List<String>> tokenKey = tokenizedKeys.get(textKey + "-token");

                    Spans<List<String>, String> pubSpans = pm.matchPubs(tokenized, tokenKey);


                    for(Span<List<String>, String> span : pubSpans.get()) {
                        List<String> spanned = span.getSpanned(tokenized);
                        String pub =  String.join(" ", spanned);

                        Map<String, Integer> countEntry = matchedPubs.computeIfAbsent(year, (e1)->new HashMap<>());
                        countEntry.computeIfAbsent(pub, e->0);
                        countEntry.put(pub, countEntry.get(pub)+1);
                    }

                }
            }
        }


        List<String> pubNames = new ArrayList<>(pubs.stream().map(p->p.name).collect(Collectors.toSet()));

        Collections.sort(pubNames);

        try(
            BufferedWriter writer = Files.newBufferedWriter(Paths.get("pubsOverTimes.csv"))
        ) {
            writer.write("year");
            writer.write(",");
            for (String name : pubNames) {
                writer.write(name);
                writer.write(",");
            }
            writer.newLine();

            List<String> years = new ArrayList<>(matchedPubs.keySet());
            Collections.sort(years);

            for (String year : years) {
                Map<String, Integer> mentioneds = matchedPubs.get(year);

                writer.write(year);
                writer.write(",");


                for (String name : pubNames) {

                    if(mentioneds.containsKey(name)) {

                        writer.write(Integer.toString(mentioneds.get(name)));
                    } else {

                        writer.write("0");
                    }

                    writer.write(",");
                }

                writer.newLine();
            }
        }
    }

    public static void process2Columns() throws Exception {

        PubMatcher pm = new PubMatcher(false, false);

        Path outDir = Paths.get("data","obNerPub");

        try {
            Files.createDirectory(outDir);
        } catch (FileAlreadyExistsException e) {
            //pass
        }

        Path start = Paths.get("data/sessionsPapers");

        Set<Path> paths = new OBFiles(start)
//            .getFiles(LocalDate.of(1800,1,1), LocalDate.of(1800,12,31));
            .getFiles(LocalDate.of(1686,1,1), LocalDate.of(1914,12,31));

        Map<Key<Spans<String, String>>, List<XML2Datum.Element>> interestingElements = new HashMap<>();

        Key<Spans<String, String>> sessionsKey = Key.of("sessionsPaper", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> trialsKey = Key.of("trialAccount", RuntimeType.stringSpans(String.class));
        Key<Spans<String, String>> statementsKey = Key.of("statement", RuntimeType.stringSpans(String.class));


        interestingElements.put(sessionsKey, ImmutableList.of(
                new XML2Datum.Element("div0", ImmutableMap.of("type", "sessionsPaper"), "sessionsPaper").isContainer(true)
        ));

        interestingElements.put(trialsKey, ImmutableList.of(
                new XML2Datum.Element("div1", ImmutableMap.of("type", "trialAccount"), "trialAccount").valueAttribute("id")
        ));

        interestingElements.put(statementsKey, ImmutableList.of(
                new XML2Datum.Element("p", ImmutableMap.of(), "statement")
        ));

        Iterator<Datum> itr = XML2Datum.getData(start, paths, interestingElements, "trialAccount", "-id").iterator();

        Key<Spans<List<String>, String>> pubSpansKey = Key.of("pubs", RuntimeType.listSpans(String.class));


        while(itr.hasNext()) {
            Datum trial = itr.next();

            KeySet keys = trial.getKeys();

            Key<String> textKey = keys.get("text");
            StringBuilder sb = new StringBuilder();

            for (Datum statement : trial.getSpannedData(statementsKey, keys)) {

                List<Datum> sents = Sentizer.sentize(statement, textKey, keys);

                for(Datum sentence : sents ) {

                    Datum tokenized = Tokenizer.tokenize(sentence, textKey, keys);
                    KeySet tokenizedKeys = tokenized.getKeys();
                    Key<List<String>> tokenKey = tokenizedKeys.get(textKey + "-token");

                    Spans<List<String>, String> pubSpans = pm.matchPubs(tokenized, tokenKey);

                    if(pubSpans.get().size() > 0){

                        tokenized = tokenized.with(pubSpansKey, pubSpans);

                        Datum2Column columns = new Datum2Column(tokenized, tokenKey, ImmutableList.of(pubSpansKey));
                        String s = columns.columnise();
                        sb.append(s);
                        sb.append("\n");
                    }
                }
            }

            if(sb.length() > 0) {

                String trialId = trial.get(trialsKey).get(0).get();
                Files.write(outDir.resolve(trialId+".col"), ImmutableList.of(sb));
            }
        }
    }

    public static void outputCleanUniquePubNames() throws Exception {
        PubMatcher pm = new PubMatcher(false, false);

        try (
                BufferedWriter writer = Files.newBufferedWriter(Paths.get("cleanUniquePubs.csv"))
        ) {
            writer.write("name,");
            writer.newLine();
            Set<Pub> pubs = new HashSet<>(pm.getPubs());
            for(Pub pub : pubs) {
                writer.write(pub.name);
                writer.newLine();
            }
        }
    }

    public static void main(String[] args ) throws Exception {

//        getUnmatched();
//        rebuildIndex();
//        getMatchedOverTime();
//        process2Columns();
        outputCleanUniquePubNames();
    }

}
