package uk.ac.susx.shl.micromacro.jdbi;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.result.ResultIterator;
import org.jdbi.v3.core.statement.Cleanable;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.StatementCustomizer;
import uk.ac.susx.tag.method51.core.data.store2.query.DatumQuery;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlFragment;
import uk.ac.susx.tag.method51.core.data.store2.query.SqlQuery;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BaseDAO<T, Q extends SqlQuery> implements DAO<T,Q> {

    private final Jdbi jdbi;
    private final RowMapper<T> mapper;
    private int fetchSize;

    public BaseDAO(Jdbi jdbi, RowMapper<T> mapper) {
        this.jdbi = jdbi;
        this.mapper = mapper;
        fetchSize = 10000;
    }

    public ResultIterator<T> iterator(Q query) {
        return jdbi.withHandle( handle -> {
                ResultIterator<T> ri = handle.createQuery(query.sql())
                            .addCustomizer(new StatementCustomizer() {
                                @Override
                                public void beforeExecution(PreparedStatement stmt, StatementContext ctx) throws SQLException {
                                    ctx.getConnection().setAutoCommit(false);
                                }
                            })
//                            .addCustomizer(new StatementCustomizer() {
//                                @Override
//                                public void afterExecution(PreparedStatement stmt, StatementContext ctx) throws SQLException {
//                                    ctx.getConnection().setAutoCommit(true);
//                                }
//                            })
                            .setFetchSize(fetchSize)
                            .map(mapper)
                            .iterator();

                final StatementContext context = ri.getContext();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        context.getConnection().setAutoCommit(true);
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                        //pass
                    }
                } ));

                ri.getContext().addCleanable(() -> context.getConnection().setAutoCommit(true));

                return ri;

            }
        );
    }

    @Override
    public Stream<T> stream(Q query){
        return jdbi.withHandle(handle -> {
                Query q = handle
                        .createQuery(query.sql())
                        .addCustomizer(new StatementCustomizer() {
                            @Override
                            public void beforeExecution(PreparedStatement stmt, StatementContext ctx) throws SQLException {
                                ctx.getConnection().setAutoCommit(false);
                            }
                        })
//                            .addCustomizer(new StatementCustomizer() {
//                                @Override
//                                public void afterExecution(PreparedStatement stmt, StatementContext ctx) throws SQLException {
//                                    ctx.getConnection().setAutoCommit(true);
//                                }
//                            })
                        .setFetchSize(fetchSize);

                final StatementContext context = q.getContext();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            context.getConnection().setAutoCommit(true);
                        } catch (SQLException e) {
                            System.out.println(e.getMessage());
                            //pass
                        }
                    } ));

                q.getContext().addCleanable(() -> context.getConnection().setAutoCommit(true));

                return q.map(mapper).stream();
            }
        );
    }

    @Override
    public int update(Q query){

        String sql = query.sql();

        return jdbi.withHandle( handle -> handle.createUpdate(sql).execute() );
    }

    @Override
    public List<T> list(Q query)  {
        Stream<T> stream = stream(query);
        List<T> list = stream.collect(Collectors.toList());
        stream.close();
        return list;
    }


    public BaseDAO<T,Q> fetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
        return this;
    }


    public int fetchSize() {
        return fetchSize;
    }
}
