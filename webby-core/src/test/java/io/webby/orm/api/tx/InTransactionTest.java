package io.webby.orm.api.tx;

import com.mockrunner.jdbc.PreparedStatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockConnection;
import io.webby.orm.api.QueryRunner;
import io.webby.orm.api.query.HardcodedSelectQuery;
import io.webby.testing.CalledOnce;
import io.webby.util.base.Unchecked;
import io.webby.util.func.ThrowConsumer;
import io.webby.util.func.ThrowFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.orm.MockingJdbc.assertThat;
import static io.webby.testing.orm.MockingJdbc.mockConnection;
import static io.webby.testing.orm.MockingJdbc.mockResultSet;
import static org.junit.jupiter.api.Assertions.*;

// FIX[minor]: more tests: force .commit() or .rollback() to fail
public class InTransactionTest {
    private MockConnection mockedConnection;
    private PreparedStatementResultSetHandler resultSetHandler;
    private InTransaction<QueryRunner> tx;

    @BeforeEach
    void setUp() {
        mockedConnection = mockConnection();
        resultSetHandler = mockedConnection.getPreparedStatementResultSetHandler();
        tx = new QueryRunner(mockedConnection).tx();
    }

    @AfterEach
    void tearDown() {
        resultSetHandler.getPreparedStatements().forEach(statement -> {
            assertTrue(statement.isClosed(), "Statement was not closed: \"%s\"".formatted(statement.getSQL()));
        });
    }

    /** {@link InTransaction#run(ThrowConsumer)} **/

    @Test
    public void run_success_with_autocommit() throws SQLException {
        mockedConnection.setAutoCommit(true);

        try (CalledOnce<QueryRunner, SQLException> calledOnce = new CalledOnce<>()) {
            tx.run(calledOnce.alsoCall(runner ->
                assertFalse(mockedConnection.getAutoCommit())
            ));
        }

        assertThat(mockedConnection).hasAutocommit(true).wasCommitted(1).wasRolledBack(0);
    }

    @Test
    public void run_success_without_autocommit() throws SQLException {
        mockedConnection.setAutoCommit(false);

        try (CalledOnce<QueryRunner, SQLException> calledOnce = new CalledOnce<>()) {
            tx.run(calledOnce.alsoCall(runner ->
                assertFalse(mockedConnection.getAutoCommit())
            ));
        }

        assertThat(mockedConnection).hasAutocommit(false).wasCommitted(1).wasRolledBack(0);
    }

    @Test
    public void run_throws_sql_exception() throws SQLException {
        mockedConnection.setAutoCommit(true);

        Exception exception = assertThrows(Exception.class, () ->
            tx.run(runner -> {
                Unchecked.throwAny(new SQLException("Fail"));
            })
        );

        assertThat(exception).hasCauseThat().hasMessageThat().isEqualTo("Fail");
        assertThat(mockedConnection).hasAutocommit(true).wasCommitted(0).wasRolledBack(1);
    }

    @Test
    public void run_throws_other_exception() throws SQLException {
        mockedConnection.setAutoCommit(true);

        AssertionError exception = assertThrows(AssertionError.class, () ->
            tx.run(runner -> {
                Unchecked.throwAny(new AssertionError("Fail"));
            })
        );

        assertThat(exception).hasMessageThat().isEqualTo("Fail");
        assertThat(mockedConnection).hasAutocommit(true).wasCommitted(0).wasRolledBack(1);
    }

    /** {@link InTransaction#run(ThrowFunction)} **/

    @Test
    public void run_returns_result() {
        resultSetHandler.prepareResultSet("select string", mockResultSet("foobar"));

        String result = tx.run(runner -> {
            return runner.runAndGetString(HardcodedSelectQuery.of("select string"));
        });
        assertThat(result).isEqualTo("foobar");

        assertThat(mockedConnection).wasCommitted(1).wasRolledBack(0);
    }
}
