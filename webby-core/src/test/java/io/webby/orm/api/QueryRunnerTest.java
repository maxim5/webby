package io.webby.orm.api;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.LongArrayList;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import io.webby.orm.api.query.Args;
import io.webby.orm.api.query.SelectWhere;
import io.webby.orm.api.query.UnresolvedArg;
import io.webby.testing.MockConsumer;
import io.webby.util.collect.Array;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.orm.api.query.Shortcuts.var;
import static io.webby.orm.testing.MockingJdbc.assertThat;
import static io.webby.orm.testing.MockingJdbc.mockConnection;
import static io.webby.orm.testing.MockingJdbc.mockPreparedStatement;
import static org.junit.jupiter.api.Assertions.*;

public class QueryRunnerTest {
    private static final Object NULL = null;
    private static final UnresolvedArg UNRESOLVED_A = new UnresolvedArg("a", 0);

    private MockConnection mockedConnection;
    private QueryRunner runner;

    @BeforeEach
    void setUp() {
        mockedConnection = mockConnection();
        runner = new QueryRunner(mockedConnection);
    }

    @Test
    public void runInTransaction_success_with_autocommit() throws SQLException {
        mockedConnection.setAutoCommit(true);

        try (MockConsumer.Tracker ignored = MockConsumer.trackAllConsumersDone()) {
            runner.runInTransaction(MockConsumer.Throw.wrap(queryRunner -> {
                assertFalse(mockedConnection.getAutoCommit());
            }));
        }

        assertTrue(mockedConnection.getAutoCommit());
        assertEquals(mockedConnection.getNumberCommits(), 1);
        assertEquals(mockedConnection.getNumberRollbacks(), 0);
    }

    @Test
    public void runInTransaction_success_without_autocommit() throws SQLException {
        mockedConnection.setAutoCommit(false);

        try (MockConsumer.Tracker ignored = MockConsumer.trackAllConsumersDone()) {
            runner.runInTransaction(MockConsumer.Throw.wrap(queryRunner -> {
                assertFalse(mockedConnection.getAutoCommit());
            }));
        }

        assertFalse(mockedConnection.getAutoCommit());
        assertEquals(mockedConnection.getNumberCommits(), 1);
        assertEquals(mockedConnection.getNumberRollbacks(), 0);
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

        assertTrue(mockedConnection.getAutoCommit());
        assertEquals(mockedConnection.getNumberCommits(), 0);
        assertEquals(mockedConnection.getNumberRollbacks(), 1);
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

        assertTrue(mockedConnection.getAutoCommit());
        assertEquals(mockedConnection.getNumberCommits(), 0);
        assertEquals(mockedConnection.getNumberRollbacks(), 1);
    }

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

    @Test
    public void setPreparedParams_objects_array() throws SQLException {
        MockPreparedStatement statement = mockPreparedStatement();
        assertEquals(QueryRunner.setPreparedParams(statement, 1, "2", 3L, 4.0f, null), 5);
        assertThat(statement).withParams().equalExactly(1, "2", 3L, 4.0f, null);
    }

    @Test
    public void setPreparedParams_objects_iterable() throws SQLException {
        MockPreparedStatement statement = mockPreparedStatement();
        assertEquals(QueryRunner.setPreparedParams(statement, Array.of(1, "2", 3L, 4.0f, null)), 5);
        assertThat(statement).withParams().equalExactly(1, "2", 3L, 4.0f, null);
    }

    @Test
    public void setPreparedParams_objects_iterable_with_offset() throws SQLException {
        MockPreparedStatement statement = mockPreparedStatement();
        statement.setDouble(1, 0.0d);
        assertEquals(QueryRunner.setPreparedParams(statement, Array.of(1, "2", 3L, 4.0f, null), 1), 6);
        assertThat(statement).withParams().equalExactly(0.0d, 1, "2", 3L, 4.0f, null);
    }

    @Test
    public void setPreparedParams_int_array() throws SQLException {
        MockPreparedStatement statement = mockPreparedStatement();
        assertEquals(QueryRunner.setPreparedParams(statement, IntArrayList.from(111, 222, 333)), 3);
        assertThat(statement).withParams().equalExactly(111, 222, 333);
    }

    @Test
    public void setPreparedParams_int_array_with_offset() throws SQLException {
        MockPreparedStatement statement = mockPreparedStatement();
        statement.setDouble(1, 0.0d);
        assertEquals(QueryRunner.setPreparedParams(statement, IntArrayList.from(111, 222, 333), 1), 4);
        assertThat(statement).withParams().equalExactly(0.0d, 111, 222, 333);
    }

    @Test
    public void setPreparedParams_long_array() throws SQLException {
        MockPreparedStatement statement = mockPreparedStatement();
        assertEquals(QueryRunner.setPreparedParams(statement, LongArrayList.from(111, 222, 333)), 3);
        assertThat(statement).withParams().equalExactly(111L, 222L, 333L);
    }

    @Test
    public void setPreparedParams_long_array_with_offset() throws SQLException {
        MockPreparedStatement statement = mockPreparedStatement();
        statement.setDouble(1, 0.0d);
        assertEquals(QueryRunner.setPreparedParams(statement, LongArrayList.from(111, 222, 333), 1), 4);
        assertThat(statement).withParams().equalExactly(0.0d, 111L, 222L, 333L);
    }
}