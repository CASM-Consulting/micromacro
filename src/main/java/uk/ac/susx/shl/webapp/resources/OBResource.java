package uk.ac.susx.shl.webapp.resources;


import com.google.gson.Gson;
import io.dropwizard.jersey.params.LocalDateParam;
import uk.ac.susx.shl.data.Match;
import uk.ac.susx.shl.data.geo.GeoJsonKnowledgeBase;
import uk.ac.susx.shl.data.text.Candidate;
import uk.ac.susx.shl.data.text.Document;
import uk.ac.susx.shl.data.text.OBTrials;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;
import uk.ac.susx.tag.method51.core.meta.KeySet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

@Path("places")
@Produces(MediaType.APPLICATION_JSON)
public class OBResource {

    private static final Logger LOG = Logger.getLogger(OBResource.class.getName());

    private final Map<LocalDate, List<Document>> trialsByDate;

    private final OBTrials obTrials;

    private final Gson gson;

    public OBResource(String geoJsonPath) throws IOException {

        obTrials = new OBTrials(geoJsonPath);
        obTrials.load();

        trialsByDate = obTrials.getDocumentsByTime();

        KeySet keys = obTrials.keys();
        gson = GsonBuilderFactory.get(keys).create();
    }

    @GET
    @Path("trials-for-date")
    public Response getTrials(@QueryParam("date") Optional<LocalDateParam> dateParam) {

        if(dateParam.isPresent()) {
            final LocalDate date = LocalDate.of(dateParam.get().get().getYear(),dateParam.get().get().getMonthOfYear(), dateParam.get().get().getDayOfMonth());

            List<Document> trials = trialsByDate.get(date);

            return Response.status(Response.Status.OK).entity(
                gson.toJson(trials)
            ).build();
        }

        return null;
    }

}
