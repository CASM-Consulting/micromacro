package uk.ac.susx.shl.micromacro.core.data.text;


import com.google.common.collect.ImmutableList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import uk.ac.susx.shl.micromacro.core.data.Match;
import uk.ac.susx.shl.micromacro.core.data.geo.GeoJsonKnowledgeBase;

import java.io.FileReader;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
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

    public PubMatcher (boolean clear) throws Exception {
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

            Pub pub = new Pub(
                trimNumbers(record.get("pub_name")),
                record.get("parish"),
                trimNumbers(record.get("pub_add_1")),
                record.get("pub_add_2"),
                record.get("pub_add_3"),
                record.get("pub_add_4"),
                record.get("pub_add_5"),
                record.get("pub_add_6")
            );

            pubs.add(pub);
        }

        return pubs;
    }

    private Map<String, List<Pub>> pub2Hash(List<Pub> pubs) {

        Map<String, List<Pub>> pubHash = new HashMap<>();

        for(Pub pub : pubs) {

            String name = trimNumbers(pub.name.toLowerCase());
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

    private Pattern leadingNumbers = Pattern.compile("^[\\d\\s\\p{Punct}]+(.*)");

    private String trimNumbers(String original) {

        String trimmed = leadingNumbers.matcher(original).replaceFirst("$1");

        return trimmed;
    }


    private void matchPubs() {

        int matched = 0;

        for(Pub pub : pubs) {

            String pubName = trimNumbers(pub.name);

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

    private class Candidate {
        List<Pub> pubs;
        String soFar;
    }

    public void matchPubs(List<String> tokens) {

        String raster = String.join(" ", tokens).toLowerCase();

        List<Candidate> candidates = new ArrayList<>();

        int i = 0;
        int j = 0;
        for(String token : tokens) {

            token = token.toLowerCase();
            if(pubHash.containsKey(token)) {

                List<Pub> pubs = pubHash.get(token);

                for(Pub pub : pubs) {

                    if(raster.startsWith(pub.name.toLowerCase(), j+i)) {
                        System.out.println(pub.name);
                        System.out.println(i);
                    }
                }

            }

            j += token.length();
            ++i;
        }

    }

    public static void main(String[] args) throws Exception {
        Set<Path> paths = new OBFiles(Paths.get("data/sessionsPapers"))
                .getFiles(LocalDate.of(1800,1,1), LocalDate.of(1800,12,31));



        PubMatcher pm = new PubMatcher(true);


        List<String> test = Arrays.asList("Drinking in the Colton Arms".split(" "));

        pm.matchPubs(test);

    }
}
