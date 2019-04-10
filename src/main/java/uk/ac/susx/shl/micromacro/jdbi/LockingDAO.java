package uk.ac.susx.shl.micromacro.jdbi;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlQuery;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class LockingDAO<T, Q extends SqlQuery> implements DAO<T, Q> {

    private static final Logger LOG = Logger.getLogger(LockingDAO.class.getName());

    private final Cache<String, Lock> running;

    private final DAO<T,Q> dao;

    public LockingDAO(DAO<T,Q> dao) {
        this.dao = dao;
        running = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public DAO<T,Q> getDAO() {
        return dao;
    }

    @Override
    public Stream<T> stream(Q query, BiFunction<Q, Object, Function<T, T>>... functions) {
        Lock lock = null;

        AtomicBoolean unlocked = new AtomicBoolean(false);

        try {
            System.out.println("acquiring lock " + Thread.currentThread().getName());
            lock = running.get(query.sql(), ReentrantLock::new);

            lock.lock();

            System.out.println(lock.toString());
            Stream<T> stream = dao.stream(query, functions);
            final Lock l = lock;
            stream.onClose(() -> {
                l.unlock();
                unlocked.set(true);
                System.out.println(l.toString());
            });

            return stream;
        } catch (ExecutionException e) {
            if(lock != null && !unlocked.get()) {
                lock.unlock();
                System.out.println(lock.toString());
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<T> list(Q query) {
        Lock lock = null;

        try {
            System.out.println("acquiring lock " + Thread.currentThread().getName());
            lock = running.get(query.sql(), ReentrantLock::new);

            lock.lock();

            System.out.println(lock.toString());
            List<T> list = dao.list(query);

            return list;
        } catch (ExecutionException e ) {
            LOG.warning(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            if(lock != null) {
                lock.unlock();
                System.out.println(lock.toString());
            }
        }
    }

    @Override
    public int update(Q query)  {
        Lock lock = null;

        try {
            System.out.println("acquiring lock " + Thread.currentThread().getName());
            lock = running.get(query.sql(), ReentrantLock::new);

            lock.lock();

            System.out.println(lock.toString());
            return dao.update(query);
        } catch (ExecutionException e) {

            throw new RuntimeException(e);
        } finally {

            if(lock != null) {
                lock.unlock();
                System.out.println(lock.toString());
            }
        }
    }
}
