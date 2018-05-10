package uk.ac.susx.shl.webapp.resources;


import uk.ac.susx.shl.data.Match;
import uk.ac.susx.shl.data.geo.GeoJsonKnowledgeBase;
import uk.ac.susx.shl.data.text.Candidate;
import uk.ac.susx.shl.data.text.Document;
import uk.ac.susx.shl.data.text.IOBColumn2Document;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

@Path("places")
@Produces(MediaType.APPLICATION_JSON)
public class PlacesResource {

    private static final Logger LOG = Logger.getLogger(PlacesResource.class.getName());

    private final GeoJsonKnowledgeBase lookup;

    public PlacesResource(String geoJsonPath) throws IOException{
        lookup = new GeoJsonKnowledgeBase(Paths.get(geoJsonPath));
    }

    @GET
    @Path("query")
    public List<Match> getPlaces(@QueryParam("name") Optional<String> name) {

        List<Match> matches = lookup.getMatches(Candidate.of(name.get()));
        return matches;
    }


    @GET
    @Path("ob")
    public List<Match> getOldBaileyPlaces(@QueryParam("name") Optional<String> name) throws IOException {
        IOBColumn2Document extractor = new IOBColumn2Document(Paths.get(name.get()));

        Iterator<Document> itr = extractor.iterator();
        List<Match> matches = new ArrayList<>();
        while(itr.hasNext()) {

            Document document = itr.next();

            for(List<Candidate> candidates : document.getCandidates("placeName")) {
                for (Candidate candidate : candidates) {

                    matches.addAll(lookup.getMatches(candidate));
                }
            }
        }
        return matches;
    }
}
