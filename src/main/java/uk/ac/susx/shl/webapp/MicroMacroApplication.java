package uk.ac.susx.shl.webapp;

import io.dropwizard.Application;
import io.dropwizard.bundles.assets.ConfiguredAssetsBundle;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import uk.ac.susx.shl.webapp.health.DefaultHealthCheck;
import uk.ac.susx.shl.webapp.resources.HelloWorldResource;
import uk.ac.susx.shl.webapp.resources.PlacesResource;

import javax.ws.rs.client.Client;
import java.io.IOException;

public class MicroMacroApplication extends Application<MicroMacroConfiguration> {

    public static void main(final String[] args) throws Exception {
        new MicroMacroApplication().run(args);
    }

    @Override
    public String getName() {
        return "MicroMacro";
    }

    @Override
    public void initialize(final Bootstrap<MicroMacroConfiguration> bootstrap) {
        bootstrap.addBundle(new ConfiguredAssetsBundle("/assets/", "/"));

    }

    @Override
    public void run(final MicroMacroConfiguration configuration,
                    final Environment environment) throws IOException {

        final PlacesResource resource = new PlacesResource(configuration.geoJsonPath);

        environment.jersey().register(resource);

        final DefaultHealthCheck healthCheck = new DefaultHealthCheck();
        environment.healthChecks().register("default", healthCheck);


        final Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration())
                .build(getName());

//        environment.jersey().register(new ExternalServiceResourace(client));

    }

}
