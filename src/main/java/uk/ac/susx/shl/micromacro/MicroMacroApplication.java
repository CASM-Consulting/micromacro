package uk.ac.susx.shl.micromacro;

import com.google.gson.Gson;
import io.dropwizard.Application;
import io.dropwizard.bundles.assets.ConfiguredAssetsBundle;
import io.dropwizard.forms.MultiPartBundle;
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
import uk.ac.susx.shl.micromacro.health.DefaultHealthCheck;
import uk.ac.susx.shl.micromacro.resources.*;
import uk.ac.susx.tag.method51.core.data.store2.query.Select;
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
        bootstrap.addBundle(new ConfiguredMicroMacroBundle());
    }

    @Override
    public void run(final MicroMacroConfiguration configuration,
                    final Environment environment) throws IOException {
    }

}
