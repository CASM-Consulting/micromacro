package uk.ac.susx.shl.micromacro.core.data.text;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import uk.ac.susx.shl.micromacro.core.data.Match;
import uk.ac.susx.shl.micromacro.core.data.geo.GeoJsonKnowledgeBase;
import uk.ac.susx.tag.method51.core.meta.Datum;
import uk.ac.susx.tag.method51.core.meta.Key;
import uk.ac.susx.tag.method51.core.meta.KeySet;
import uk.ac.susx.tag.method51.core.meta.span.Spans;
import uk.ac.susx.tag.method51.core.meta.types.RuntimeType;

import java.io.FileReader;
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

public class PubMatcher {
    private static final Logger LOG = Logger.getLogger(PubMatcher.class.getName());


    public static class Pub implements Serializable {
        public final String name;
        public final List<String> addr;
        public final String parish;

        public Match match;

        public Pub(String name, String parish, String addr1, String addr2, String addr3, String addr4, String addr5, String addr6) {
            this.name = name;
            this.parish = parish;
            addr = ImmutableList.of(addr1, addr2, addr3, addr4, addr5, addr6);
        }
    }


    private final GeoJsonKnowledgeBase lookup;

    private List<Pub> pubs;

    private Map<String, List<Pub>> pubHash;

    private boolean lc;

    public PubMatcher (boolean clear, boolean lc) throws Exception {
        DB db = DBMaker
                .fileDB("data/pubDB")
                .fileMmapEnable()
                .closeOnJvmShutdown()
//                .readOnly()
                .make();


        lookup = new GeoJsonKnowledgeBase(Paths.get("LL_PL_PA_WA_POINTS_FeaturesT.json"));

//        pubs = (List) db.indexTreeList("pubs").createOrOpen();
//        pubHash = (Map)db.hashMap("trials-by-date").createOrOpen();

        pubs = new ArrayList<>();
        pubHash = new HashMap<>();

        this.lc = lc;

        if(clear) {
            pubs.clear();
            pubs.addAll(csv2Pubs("oldbailey-alex_1800-pubs.csv"));

            pubHash.clear();
            pubHash.putAll(pub2Hash(pubs));
        }

    }

    private List<Pub> csv2Pubs(String path) throws Exception {

        Iterable<CSVRecord> records = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(new FileReader(path));

        List<Pub> pubs = new ArrayList<>();

        for(CSVRecord record : records) {

            String name = record.get("pub_name");
            name = trimNumbers(name);

            Pub pub = new Pub(
                name,
                record.get("parish"),
                trimNumbers(record.get("pub_add_1")),
                record.get("pub_add_2"),
                record.get("pub_add_3"),
                record.get("pub_add_4"),
                record.get("pub_add_5"),
                record.get("pub_add_6")
            );
            pubs.add(pub);

            Matcher m = andPattern.matcher(name);
            if(m.find()) {
                String andName = m.replaceAll("&");
                Pub andPub = new Pub(
                        andName,
                        record.get("parish"),
                        trimNumbers(record.get("pub_add_1")),
                        record.get("pub_add_2"),
                        record.get("pub_add_3"),
                        record.get("pub_add_4"),
                        record.get("pub_add_5"),
                        record.get("pub_add_6")
                );
                pubs.add(andPub);
            }

            m = ampersandPattern.matcher(name);
            if(m.find()) {
                String andName = m.replaceAll("and");
                Pub andPub = new Pub(
                        andName,
                        record.get("parish"),
                        trimNumbers(record.get("pub_add_1")),
                        record.get("pub_add_2"),
                        record.get("pub_add_3"),
                        record.get("pub_add_4"),
                        record.get("pub_add_5"),
                        record.get("pub_add_6")
                );
                pubs.add(andPub);
            }
        }

        return pubs;
    }

    private Map<String, List<Pub>> pub2Hash(List<Pub> pubs) {

        Map<String, List<Pub>> pubHash = new HashMap<>();

        for(Pub pub : pubs) {

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

    private String trimNumbers(String original) {

        String trimmed = leadingNumbers.matcher(original).replaceFirst("$1");

        return trimmed;
    }


    private void matchPubs() {

        int matched = 0;

        for(Pub pub : pubs) {

            String pubName = pub.name;

            List<Match> candidates = lookup.getMatches(pubName);

            if(candidates.isEmpty()) {

                String addr = trimNumbers(pub.addr.get(0));

                candidates = lookup.getMatches(addr);
            }

            if(candidates.isEmpty()) {

                String addr = trimNumbers(pub.addr.get(1));

                candidates = lookup.getMatches(addr);
            }

            if(!candidates.isEmpty()) {
                pub.match = candidates.get(0);
                ++matched;
            }

        }

        LOG.info(matched + " of " + pubs.size() + " matched" );
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

                        pubSpans = pubSpans.with(indexMap.get(j+i), indexMap.get(j+i+candidates.get(0).name.length()-1), "pub");
                    }


                }
            }

            j += token.length();
            ++i;

            pubSearch = token.equalsIgnoreCase("the");
        }

        return pubSpans;
    }

    public static void main(String[] args) throws Exception {

        PubMatcher pm = new PubMatcher(true, false);


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
                    Key<List<String>> tokenKey = tokenizedKeys.get(textKey + Tokenizer.SUFFIX);

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
}
