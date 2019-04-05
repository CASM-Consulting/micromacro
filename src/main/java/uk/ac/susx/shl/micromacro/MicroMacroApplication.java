package uk.ac.susx.shl.micromacro;

import com.google.gson.Gson;
import io.dropwizard.Application;
import io.dropwizard.bundles.assets.ConfiguredAssetsBundle;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.eclipse.jetty.server.session.SessionHandler;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlLogger;
import org.jdbi.v3.core.statement.StatementContext;
import uk.ac.susx.shl.micromacro.core.*;
import uk.ac.susx.shl.micromacro.jdbi.*;
import uk.ac.susx.shl.micromacro.core.data.text.PubMatcher;
import uk.ac.susx.shl.micromacro.health.DefaultHealthCheck;
import uk.ac.susx.shl.micromacro.resources.*;
import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlQuery;
import uk.ac.susx.tag.method51.core.gson.GsonBuilderFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class MicroMacroApplication extends Application<MicroMacroConfiguration> {
    private static final Logger LOG = Logger.getLogger(MicroMacroApplication.class.getName());

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

        environment.servlets().setSessionHandler(new SessionHandler());

        environment.jersey().register(new JsonProcessingExceptionMapper(true));
        environment.jersey().register(GsonMessageBodyHandler.class);

        //old stuff
//        final PubMatcher pubMatcher = new PubMatcher(false, false);
//        final PlacesResource places = new PlacesResource(configuration.geoJsonPath, pubMatcher);
//        environment.jersey().register(places);
//        final OBResource ob = new OBResource(configuration.sessionsPath, configuration.geoJsonPath,
//                configuration.obMapPath, configuration.obCacheTable, jdbi,
//                configuration.placeNerPort, configuration.pubNerPort,
//                pubMatcher);
//        environment.jersey().register(ob);

        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, configuration.getDataSourceFactory(), "postgresql");

        jdbi.setSqlLogger(new SqlLogger() {
            @Override
            public void logBeforeExecution(StatementContext context) {
                LOG.info(context.getRenderedSql());
            }

            @Override
            public void logAfterExecution(StatementContext context) {
                LOG.info("done: " + context.getRenderedSql());
            }
        });

        final Method52DAO method52DAO = new Method52DAO(jdbi);

        final Gson gson = GsonBuilderFactory.get()/*.registerTypeAdapterFactory(StreamTypeAdapter.get())*/.create();
        final DatumDAO datumDAO = new DatumDAO(jdbi, method52DAO);

        final QueryFactory queryFactory = new QueryFactory(gson);
        Files.createDirectories(Paths.get("data"));


        final DefaultHealthCheck healthCheck = new DefaultHealthCheck();
        environment.healthChecks().register("default", healthCheck);

//        final Client client = new JerseyClientBuilder(environment).using(configuration.getJerseyClientConfiguration()).build(getName());

        environment.jersey().register(new Method52Resouce(method52DAO));

//        QueryResultCache cache = new QueryResultCache(configuration.resultsCachePath);

//        environment.jersey().register(new QueryResources(datumDAO, cache));

        DAO<String, SqlQuery> lockingCachingDatumDAO = new LockingDAO<>(new CachingDAO<>(new BaseDAO<>(jdbi, new DatumStringMapper()), configuration.resultsCachePath));

        environment.jersey().register(new QueryResources2(lockingCachingDatumDAO));

        environment.jersey().register(new TableResource(method52DAO));

        final WorkspaceFactory workspaceFactory = new WorkspaceFactory(queryFactory, configuration.historical);

        final Workspaces workspaces = new Workspaces(configuration.workspaceMapPath, workspaceFactory);

        environment.jersey().register(new WorkspacesResource(workspaces, workspaceFactory));

        environment.jersey().register(new WorkspaceResource(workspaces, queryFactory/*, cache*/));
    }

}
