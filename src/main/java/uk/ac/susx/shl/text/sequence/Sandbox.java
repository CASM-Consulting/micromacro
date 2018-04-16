package uk.ac.susx.shl.text.sequence;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sw206 on 16/04/2018.
 */
public class Sandbox {

    public static void main(String[] args) throws Exception {

        GeoJsonKnowledgeBase knowledgeBase = new GeoJsonKnowledgeBase(Paths.get("LL_PL_PA_WA_POINTS_FeaturesT.json"));

        IOBColumnCandidateExtractor extractor = new IOBColumnCandidateExtractor(Paths.get("placeName.out"));

        Iterator<Candidate> itr = extractor.iterator();

        while(itr.hasNext()) {

            Candidate candidate = itr.next();

            List<Match> matches = knowledgeBase.getMatches(candidate);

            System.out.println(candidate.getText());

            for(Match match : matches) {


                System.out.println(match.toString());
            }

            System.out.println("================================================");

        }

    }



}
