package uk.ac.susx.shl.micromacro.webapp.resources;


import com.google.gson.Gson;
import io.dropwizard.jersey.jsr310.LocalDateParam;
import io.dropwizard.jersey.jsr310.LocalDateTimeParam;
import org.jdbi.v3.core.Jdbi;
import uk.ac.susx.shl.micromacro.core.data.text.OBTrials;
import uk.ac.susx.shl.micromacro.core.data.text.SimpleDocument;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;
import uk.ac.susx.tag.method51.core.meta.KeySet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;

@Path("ob")
@Produces(MediaType.APPLICATION_JSON)
public class OBResource {

    private static final Logger LOG = Logger.getLogger(OBResource.class.getName());

//    private final Map<LocalDate, List<SimpleDocument>> trialsByDate;
    private final Map<String, SimpleDocument> trialsId;

    private final OBTrials obTrials;

    private final Gson gson;

    public OBResource(String sessionsPath, String geoJsonPath, String obMapPath, String obCacheTable, Jdbi jdbi) throws IOException {

        obTrials = new OBTrials(sessionsPath, geoJsonPath, obMapPath);


//        trialsByDate = obTrials.getDocumentsByDate();
        trialsId = obTrials.getDocumentsById();

        KeySet keys = obTrials.keys();
        gson = GsonBuilderFactory.get(keys).create();
    }

    @GET
    @Path("trials-by-date")
    public Response trialsByDate(@QueryParam("date") Optional<LocalDateParam> dateParam) {

        if(dateParam.isPresent()) {
            final LocalDate date = dateParam.get().get();

            List<SimpleDocument> trials = obTrials.getDocumentsByDate(date, date.plusDays(1)).get(date);

            return Response.status(Response.Status.OK).entity(
                gson.toJson(trials)
            ).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

    }

    @GET
    @Path("trials-by-id")
    public Response trialsById(@QueryParam("id") String idParam) {

        final String id = idParam;

        SimpleDocument trial = obTrials.getDocumentsById().get(id);

        return Response.status(Response.Status.OK).entity(
                gson.toJson(trial)
        ).build();

    }

    @GET
    @Path("matches")
    public Response matches() {

        List<Map<String, String>> matches = obTrials.getMatches();

        return Response.status(Response.Status.OK).entity(
                gson.toJson(matches)
        ).build();
    }


    @GET
    @Path("load")
    public Response load(@QueryParam("from") LocalDateParam from, @QueryParam("to") LocalDateParam to) {

        obTrials.load(from.get(), to.get());
        List<Map<String, String>> matches = obTrials.getMatches();

        return Response.status(Response.Status.OK).entity(
                gson.toJson(matches)
        ).build();
    }

}
