package uk.ac.susx.shl.micromacro.db;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Environment;
import org.jdbi.v3.core.Jdbi;
import uk.ac.susx.shl.micromacro.MicroMacroConfiguration;
import uk.ac.susx.shl.micromacro.webapp.resources.Method52Resouce;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by sw206 on 19/07/2018.
 */
public class JdbiProvider {

    private final DataSourceFactory dataSourceFactory;
    private final Environment environment;
    private final String url;

    private final JdbiFactory factory;

    private final ReadWriteLock locks = new ReentrantReadWriteLock();


    public JdbiProvider(final DataSourceFactory dataSourceFactory,
                        final Environment environment) {

        this.dataSourceFactory = dataSourceFactory;
        this.environment = environment;
        factory = new JdbiFactory();
        url = dataSourceFactory.getUrl();
        //create one for the health check
        factory.build(environment, dataSourceFactory, "postgresql");
    }

    public Jdbi get() {
        return get("");
    }

    public Jdbi get(String database) {

        Lock lock = locks.writeLock();

        try {
            lock.lock();

            String dbUrl = this.url + database;

            dataSourceFactory.setUrl(dbUrl);
            //bypass health check creation
            ManagedDataSource dataSource = dataSourceFactory.build(environment.metrics(), "postgresql"+database);

            Jdbi jdbi = Jdbi.create(dataSource);

            return jdbi;
        } finally {
            dataSourceFactory.setUrl(url);
            lock.unlock();
        }
    }
}
