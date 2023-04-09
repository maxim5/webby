package io.webby.orm.api;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.LongArrayList;
import com.mockrunner.jdbc.PreparedStatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import com.mockrunner.mock.jdbc.MockResultSet;
import io.webby.orm.api.query.*;
import io.webby.testing.CalledOnce;
import io.webby.util.collect.Array;
import io.webby.util.func.ThrowConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.orm.api.query.Shortcuts.var;
import static io.webby.orm.testing.MockingJdbc.assertThat;
import static io.webby.orm.testing.MockingJdbc.mockConnection;
import static io.webby.orm.testing.MockingJdbc.mockPreparedStatement;
import static io.webby.orm.testing.MockingJdbc.mockResultSet;
import static org.junit.jupiter.api.Assertions.*;

// FIX[minor]: more tests: force .commit() or .rollback() to fail
// FIX[norm]: more tests: .runAndGetInt() result type mismatch
public class QueryRunnerTest {
    private static final Object NULL = null;
    private static final UnresolvedArg UNRESOLVED_A = new UnresolvedArg("a", 0);

    private MockConnection mockedConnection;
    private QueryRunner runner;
    private PreparedStatementResultSetHandler resultSetHandler;
    private MockPreparedStatement mockStatement;

    @BeforeEach
    void setUp() {
        mockedConnection = mockConnection();
        resultSetHandler = mockedConnection.getPreparedStatementResultSetHandler();
        mockStatement = mockPreparedStatement();
        runner = new QueryRunner(mockedConnection);
    }

    /** {@link QueryRunner#runInTransaction(ThrowConsumer)} **/

    @Test
    public void runInTransaction_success_with_autocommit() throws SQLException {
        mockedConnection.setAutoCommit(true);

        try (CalledOnce<QueryRunner, SQLException> calledOnce = new CalledOnce<>()) {
            runner.runInTransaction(calledOnce.alsoCall(queryRunner ->
                assertFalse(mockedConnection.getAutoCommit())
            ));
        }

        assertThat(mockedConnection).hasAutocommit(true).wasCommitted(1).wasRolledBack(0);
    }

    @Test
    public void runInTransaction_success_without_autocommit() throws SQLException {
        mockedConnection.setAutoCommit(false);

        try (CalledOnce<QueryRunner, SQLException> calledOnce = new CalledOnce<>()) {
            runner.runInTransaction(calledOnce.alsoCall(queryRunner ->
                assertFalse(mockedConnection.getAutoCommit())
            ));
        }

        assertThat(mockedConnection).hasAutocommit(false).wasCommitted(1).wasRolledBack(0);
    }

    @Test
    public void runInTransaction_throws_sql_exception() throws SQLException {
        mockedConnection.setAutoCommit(true);

        Exception exception = assertThrows(SQLException.class, () ->
            runner.runInTransaction(queryRunner -> {
                throw new SQLException("Fail");
            })
        );

        assertThat(exception).hasMessageThat().isEqualTo("Fail");
        assertThat(mockedConnection).hasAutocommit(true).wasCommitted(0).wasRolledBack(1);
    }

    @Test
    public void runInTransaction_throws_other_exception() throws SQLException {
        mockedConnection.setAutoCommit(true);

        AssertionError exception = assertThrows(AssertionError.class, () ->
            runner.runInTransaction(queryRunner -> {
                throw new AssertionError("Fail");
            })
        );

        assertThat(exception).hasMessageThat().isEqualTo("Fail");
        assertThat(mockedConnection).hasAutocommit(true).wasCommitted(0).wasRolledBack(1);
    }

    /** {@link QueryRunner#runAndGet(SelectQuery)} **/

    @Test
    public void runAndGet_one_result() {
        resultSetHandler.clearResultSets();
        assertThat(runner.runAndGet(HardcodedSelectQuery.of("select x"))).isNull();

        resultSetHandler.prepareResultSet("select str", mockResultSet("foo"));
        assertThat(runner.runAndGet(HardcodedSelectQuery.of("select str"))).isEqualTo("foo");

        resultSetHandler.prepareResultSet("select null", mockResultSet(NULL));
        assertThat(runner.runAndGet(HardcodedSelectQuery.of("select null"))).isEqualTo(NULL);
    }

    @Test
    public void runAndGet_several_results() {
        resultSetHandler.prepareResultSet("select many", mockResultSet("foo", "bar"));
        assertThat(runner.runAndGet(HardcodedSelectQuery.of("select many"))).isEqualTo("foo");

        resultSetHandler.prepareResultSet("select more", mockResultSet(NULL, "foo", "bar"));
        assertThat(runner.runAndGet(HardcodedSelectQuery.of("select more"))).isEqualTo(NULL);
    }

    @Test
    public void runAndGet_fails() {
        resultSetHandler.prepareThrowsSQLException("select fail");
        QueryException exception = assertThrows(QueryException.class, () ->
            runner.runAndGet(HardcodedSelectQuery.of("select fail"))
        );
        assertThat(exception.getQuery()).isEqualTo("select fail");
        assertThat(exception.getArgs()).isEmpty();
    }

    /** {@link QueryRunner#runAndGetString(SelectQuery)} **/

    @Test
    public void runAndGetString_one_result() {
        resultSetHandler.clearResultSets();
        assertThat(runner.runAndGetString(HardcodedSelectQuery.of("select x"))).isNull();

        resultSetHandler.prepareResultSet("select str", mockResultSet("foo"));
        assertThat(runner.runAndGetString(HardcodedSelectQuery.of("select str"))).isEqualTo("foo");

        resultSetHandler.prepareResultSet("select null", mockResultSet(NULL));
        assertThat(runner.runAndGetString(HardcodedSelectQuery.of("select null"))).isEqualTo(NULL);
    }

    @Test
    public void runAndGetString_several_results() {
        resultSetHandler.prepareResultSet("select many", mockResultSet("foo", "bar"));
        assertThat(runner.runAndGetString(HardcodedSelectQuery.of("select many"))).isEqualTo("foo");

        resultSetHandler.prepareResultSet("select more", mockResultSet(NULL, "foo", "bar"));
        assertThat(runner.runAndGetString(HardcodedSelectQuery.of("select more"))).isEqualTo(NULL);
    }

    @Test
    public void runAndGetString_fails() {
        resultSetHandler.prepareThrowsSQLException("select fail", List.of(777));
        QueryException exception = assertThrows(QueryException.class, () ->
            runner.runAndGetString(HardcodedSelectQuery.of("select fail", Args.of(777)))
        );
        assertThat(exception.getQuery()).isEqualTo("select fail");
        assertThat(exception.getArgs()).containsExactly(777);
    }

    /** {@link QueryRunner#runAndGetInt(SelectQuery, int)} **/

    @Test
    public void runAndGetInt_one_result() {
        resultSetHandler.clearResultSets();
        assertThat(runner.runAndGetInt(HardcodedSelectQuery.of("select x"), -1)).isEqualTo(-1);

        resultSetHandler.prepareResultSet("select int", mockResultSet(555));
        assertThat(runner.runAndGetInt(HardcodedSelectQuery.of("select int"), -1)).isEqualTo(555);

        resultSetHandler.prepareResultSet("select 0", mockResultSet(0));
        assertThat(runner.runAndGetInt(HardcodedSelectQuery.of("select 0"), -1)).isEqualTo(0);
    }

    @Test
    public void runAndGetInt_several_results() {
        resultSetHandler.prepareResultSet("select many", mockResultSet(111, 222));
        assertThat(runner.runAndGetInt(HardcodedSelectQuery.of("select many"), -1)).isEqualTo(111);

        resultSetHandler.prepareResultSet("select more", mockResultSet(0, 111, 222));
        assertThat(runner.runAndGetInt(HardcodedSelectQuery.of("select more"), -1)).isEqualTo(0);
    }

    @Test
    public void runAndGetInt_fails() {
        resultSetHandler.prepareThrowsSQLException("select fail", Array.of(NULL));
        QueryException exception = assertThrows(QueryException.class, () ->
            runner.runAndGetInt(HardcodedSelectQuery.of("select fail", Args.of(NULL)), -1)
        );
        assertThat(exception.getQuery()).isEqualTo("select fail");
        assertThat(exception.getArgs()).containsExactly(NULL);
    }

    /** {@link QueryRunner#runAndGetLong(SelectQuery, long)} **/

    @Test
    public void runAndGetLong_one_result() {
        resultSetHandler.clearResultSets();
        assertThat(runner.runAndGetLong(HardcodedSelectQuery.of("select x"), -1L)).isEqualTo(-1L);

        resultSetHandler.prepareResultSet("select int", mockResultSet(555));
        assertThat(runner.runAndGetLong(HardcodedSelectQuery.of("select int"), -1L)).isEqualTo(555);

        resultSetHandler.prepareResultSet("select 0", mockResultSet(0));
        assertThat(runner.runAndGetLong(HardcodedSelectQuery.of("select 0"), -1L)).isEqualTo(0);
    }

    @Test
    public void runAndGetLong_several_results() {
        resultSetHandler.prepareResultSet("select many", mockResultSet(111, 222));
        assertThat(runner.runAndGetLong(HardcodedSelectQuery.of("select many"), -1L)).isEqualTo(111);

        resultSetHandler.prepareResultSet("select more", mockResultSet(0, 111, 222));
        assertThat(runner.runAndGetLong(HardcodedSelectQuery.of("select more"), -1L)).isEqualTo(0);
    }

    @Test
    public void runAndGetLong_fails() {
        resultSetHandler.prepareThrowsSQLException("select fail", Array.of(777, "foo"));
        QueryException exception = assertThrows(QueryException.class, () ->
            runner.runAndGetLong(HardcodedSelectQuery.of("select fail", Args.of(777, "foo")), -1L)
        );
        assertThat(exception.getQuery()).isEqualTo("select fail");
        assertThat(exception.getArgs()).containsExactly(777, "foo");
    }

    /** {@link QueryRunner#run(SelectQuery, ThrowConsumer)} **/

    @Test
    public void run_ok() {
        MockResultSet resultSet = mockResultSet(777);
        resultSetHandler.prepareResultSet("select me", resultSet);

        CalledOnce<ResultSet, SQLException> calledOnce = new CalledOnce<>();
        runner.run(HardcodedSelectQuery.of("select me"), calledOnce);
        assertThat(calledOnce.getValue()).isIdenticalTo(resultSet);
    }

    @Test
    public void run_fails() {
        resultSetHandler.prepareThrowsSQLException("select fail", Array.of(777));
        QueryException exception = assertThrows(QueryException.class, () ->
            runner.run(HardcodedSelectQuery.of("select fail", Args.of(777)), resultSet -> fail("Must not be called"))
        );
        assertThat(exception.getQuery()).isEqualTo("select fail");
        assertThat(exception.getArgs()).containsExactly(777);
    }

    /** {@link QueryRunner#prepareQuery} **/

    @Test
    public void prepareQuery_simple() throws SQLException {
        try (PreparedStatement statement = runner.prepareQuery("select 0")) {
            assertThat(statement).queryEquals("select 0").hasNoParams();
        }
    }

    @Test
    public void prepareQuery_with_args_resolved() throws SQLException {
        try (PreparedStatement statement = runner.prepareQuery("select args", Args.of(1, 2))) {
            assertThat(statement)
                .queryEquals("select args")
                .withParams().equalExactly(1, 2);
        }
    }

    @Test
    @SuppressWarnings("resource")
    public void prepareQuery_with_args_unresolved_fails() {
        assertThrows(AssertionError.class, () -> runner.prepareQuery("select NULL", Args.of(1, 2, UNRESOLVED_A)));
    }

    @Test
    public void prepareQuery_with_single_null_object() throws SQLException {
        try (PreparedStatement statement = runner.prepareQuery("select obj", NULL)) {
            assertThat(statement)
                .queryEquals("select obj")
                .withParams().equalExactly(NULL);
        }
    }

    @Test
    public void prepareQuery_with_single_int() throws SQLException {
        try (PreparedStatement statement = runner.prepareQuery("select int", 111)) {
            assertThat(statement)
                .queryEquals("select int")
                .withParams().equalExactly(111);
        }
    }

    @Test
    public void prepareQuery_with_single_long() throws SQLException {
        try (PreparedStatement statement = runner.prepareQuery("select long", 777L)) {
            assertThat(statement)
                .queryEquals("select long")
                .withParams().equalExactly(777L);
        }
    }

    @Test
    public void prepareQuery_with_two_objects() throws SQLException {
        try (PreparedStatement statement = runner.prepareQuery("select two", 1f, NULL)) {
            assertThat(statement)
                .queryEquals("select two")
                .withParams().equalExactly(1f, NULL);
        }
    }

    @Test
    public void prepareQuery_with_three_objects() throws SQLException {
        try (PreparedStatement statement = runner.prepareQuery("select three", 1f, 2L, NULL)) {
            assertThat(statement)
                .queryEquals("select three")
                .withParams().equalExactly(1f, 2L, NULL);
        }
    }

    @Test
    public void prepareQuery_with_array_of_objects() throws SQLException {
        try (PreparedStatement statement = runner.prepareQuery("select array", 1f, 2L, "3", 4, NULL)) {
            assertThat(statement)
                .queryEquals("select array")
                .withParams().equalExactly(1f, 2L, "3", 4, NULL);
        }
    }

    @Test
    public void prepareQuery_with_iterable_of_objects() throws SQLException {
        try (PreparedStatement statement = runner.prepareQuery("select it", Array.of(1f, 2L, "3", 4, NULL))) {
            assertThat(statement)
                .queryEquals("select it")
                .withParams().equalExactly(1f, 2L, "3", 4, NULL);
        }
    }

    @Test
    public void prepareQuery_with_int_array() throws SQLException {
        try (PreparedStatement statement = runner.prepareQuery("select ints", IntArrayList.from(111, 222))) {
            assertThat(statement)
                .queryEquals("select ints")
                .withParams().equalExactly(111, 222);
        }
    }

    @Test
    public void prepareQuery_with_long_array() throws SQLException {
        try (PreparedStatement statement = runner.prepareQuery("select longs", LongArrayList.from(111, 222))) {
            assertThat(statement)
                .queryEquals("select longs")
                .withParams().equalExactly(111L, 222L);
        }
    }

    @Test
    public void prepareQuery_with_query() throws SQLException {
        try (PreparedStatement statement = runner.prepareQuery(SelectWhere.from("foo").select(var(777L)).build())) {
            assertThat(statement)
                .queryEquals("""
                             SELECT ?
                             FROM foo
                             """)
                .withParams().equalExactly(777L);
        }
    }

    /** {@link QueryRunner#setPreparedParams} **/

    @Test
    public void setPreparedParams_objects_array() throws SQLException {
        assertEquals(QueryRunner.setPreparedParams(mockStatement, 1, "2", 3L, 4.0f, null), 5);
        assertThat(mockStatement).withParams().equalExactly(1, "2", 3L, 4.0f, null);
    }

    @Test
    public void setPreparedParams_objects_iterable() throws SQLException {
        assertEquals(QueryRunner.setPreparedParams(mockStatement, Array.of(1, "2", 3L, 4.0f, null)), 5);
        assertThat(mockStatement).withParams().equalExactly(1, "2", 3L, 4.0f, null);
    }

    @Test
    public void setPreparedParams_objects_iterable_with_offset() throws SQLException {
        mockStatement.setDouble(1, 0.0d);
        assertEquals(QueryRunner.setPreparedParams(mockStatement, Array.of(1, "2", 3L, 4.0f, null), 1), 6);
        assertThat(mockStatement).withParams().equalExactly(0.0d, 1, "2", 3L, 4.0f, null);
    }

    @Test
    public void setPreparedParams_int_array() throws SQLException {
        assertEquals(QueryRunner.setPreparedParams(mockStatement, IntArrayList.from(111, 222, 333)), 3);
        assertThat(mockStatement).withParams().equalExactly(111, 222, 333);
    }

    @Test
    public void setPreparedParams_int_array_with_offset() throws SQLException {
        mockStatement.setDouble(1, 0.0d);
        assertEquals(QueryRunner.setPreparedParams(mockStatement, IntArrayList.from(111, 222, 333), 1), 4);
        assertThat(mockStatement).withParams().equalExactly(0.0d, 111, 222, 333);
    }

    @Test
    public void setPreparedParams_long_array() throws SQLException {
        assertEquals(QueryRunner.setPreparedParams(mockStatement, LongArrayList.from(111, 222, 333)), 3);
        assertThat(mockStatement).withParams().equalExactly(111L, 222L, 333L);
    }

    @Test
    public void setPreparedParams_long_array_with_offset() throws SQLException {
        mockStatement.setDouble(1, 0.0d);
        assertEquals(QueryRunner.setPreparedParams(mockStatement, LongArrayList.from(111, 222, 333), 1), 4);
        assertThat(mockStatement).withParams().equalExactly(0.0d, 111L, 222L, 333L);
    }
}