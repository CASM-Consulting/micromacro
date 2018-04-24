package uk.ac.susx.shl.webapp.resources;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

/**
 * Created by sw206 on 18/04/2018.
 */
@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {


    private final String template;

    public HelloWorldResource() {

        template = "hello %s";

    }

    @GET
    @Timed
    public String sayHello(@QueryParam("name") Optional<String> name) {
        final String value = String.format(template, name.orElse("Dave"));
        return value;
    }
}
