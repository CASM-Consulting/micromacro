package uk.ac.susx.shl.webapp;

import io.dropwizard.Application;
import io.dropwizard.bundles.assets.ConfiguredAssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.ac.susx.shl.webapp.health.DefaultHealthCheck;
import uk.ac.susx.shl.webapp.resources.HelloWorldResource;

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
                    final Environment environment) {
        final HelloWorldResource resource = new HelloWorldResource();
        environment.jersey().register(resource);

        final DefaultHealthCheck healthCheck =
                new DefaultHealthCheck();
        environment.healthChecks().register("default", healthCheck);
    }

}
