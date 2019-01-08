package uk.ac.susx.shl.micromacro;

import io.dropwizard.Application;
import io.dropwizard.bundles.assets.ConfiguredAssetsBundle;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.jdbi.v3.core.Jdbi;
import uk.ac.susx.shl.micromacro.db.DatumWrapperDAO;
import uk.ac.susx.shl.micromacro.db.Method52DAO;
import uk.ac.susx.shl.micromacro.core.data.text.PubMatcher;
import uk.ac.susx.shl.micromacro.webapp.health.DefaultHealthCheck;
import uk.ac.susx.shl.micromacro.webapp.resources.DatumResources;
import uk.ac.susx.shl.micromacro.webapp.resources.Method52Resouce;
import uk.ac.susx.shl.micromacro.webapp.resources.OBResource;
import uk.ac.susx.shl.micromacro.webapp.resources.PlacesResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

        Files.createDirectories(Paths.get("data"));

        JdbiFactory factory = new JdbiFactory();

        Jdbi jdbi = factory.build(environment, configuration.getDataSourceFactory(), "postgresql");
//        Jdbi jdbi = null;

        PubMatcher pubMatcher = new PubMatcher(false, false);
        Method52DAO method52Data = new Method52DAO(jdbi);

        final PlacesResource places = new PlacesResource(configuration.geoJsonPath, pubMatcher);
        environment.jersey().register(places);

        final OBResource ob = new OBResource(configuration.sessionsPath, configuration.geoJsonPath,
                configuration.obMapPath, configuration.obCacheTable, jdbi,
                configuration.placeNerPort, configuration.pubNerPort,
                pubMatcher);

        environment.jersey().register(ob);

        final DefaultHealthCheck healthCheck = new DefaultHealthCheck();
        environment.healthChecks().register("default", healthCheck);

//        final Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration()).build(getName());

        environment.jersey().register(new Method52Resouce(method52Data));


        final DatumWrapperDAO dao = new DatumWrapperDAO(jdbi);
        environment.jersey().register(new DatumResources(dao));

    }

}
