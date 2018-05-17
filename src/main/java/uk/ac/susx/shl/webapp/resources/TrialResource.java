package uk.ac.susx.shl.webapp.resources;


import io.dropwizard.jersey.params.LocalDateParam;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Path("places")
@Produces(MediaType.APPLICATION_JSON)
public class TrialResource {

    private static final Logger LOG = Logger.getLogger(TrialResource.class.getName());


    public TrialResource(String geoJsonPath) throws IOException {


    }

    @GET
    @Path("trials")
    public List<Match> getTrials(@QueryParam("from") Optional<LocalDateParam> fromParam, @QueryParam("to") Optional<LocalDateParam> toParam) {

        final LocalDateParam to = toParam.get();
        final LocalDateParam from = fromParam.get();




        return null;
    }

}
