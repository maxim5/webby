package io.webby.orm.testing;

import com.google.common.truth.IterableSubject;
import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.mockrunner.mock.jdbc.*;
import io.webby.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static io.webby.testing.AssertBasics.getPrivateFieldValue;
import static io.webby.util.base.EasyCast.castAny;

public class MockingJdbc {
    public static @NotNull String uniqueId() {
        return DateTimeFormatter.ofPattern("'['HH:mm:ss.SSSSSS']'").format(LocalTime.now());
    }

    public static @NotNull MockConnection mockConnection() {
        return new MockConnection();
    }

    public static @NotNull MockPreparedStatement mockPreparedStatement() {
        return mockPreparedStatement(null);
    }

    public static @NotNull MockPreparedStatement mockPreparedStatement(@Nullable String query) {
        return new MockPreparedStatement(mockConnection(), query);
    }

    public static @NotNull MockResultSet mockResultSet(@Nullable Object @NotNull ... row) {
        MockResultSet mockResultSet = new MockResultSet(uniqueId());
        mockResultSet.addRow(row);
        return mockResultSet;
    }

    public static @NotNull MockResultSet mockResultSet(@NotNull List<Object[]> rows) {
        MockResultSet mockResultSet = new MockResultSet(uniqueId());
        for (Object[] row : rows) {
            mockResultSet.addRow(row);
        }
        return mockResultSet;
    }

    public static @NotNull MockConnectionSubject assertThat(@NotNull MockConnection connection) {
        return new MockConnectionSubject(connection);
    }

    public static @NotNull MockMockPreparedStatementSubject assertThat(@NotNull MockPreparedStatement statement) {
        return new MockMockPreparedStatementSubject(statement);
    }

    public static @NotNull MockMockPreparedStatementSubject assertThat(@NotNull PreparedStatement statement) {
        return assertThat(((MockPreparedStatement) statement));
    }

    public static @NotNull MockParameterMapSubject assertThat(@NotNull MockParameterMap parameterMap) {
        return new MockParameterMapSubject(parameterMap);
    }

    public static @NotNull MockResultSetSubject assertThat(@NotNull MockResultSet resultSet) {
        return new MockResultSetSubject(resultSet);
    }

    public static @NotNull MockResultSetSubject assertThat(@NotNull ResultSet resultSet) {
        return assertThat(((MockResultSet) resultSet));
    }

    @CanIgnoreReturnValue
    public record MockConnectionSubject(@NotNull MockConnection connection) {
        public IterableSubject executedQueries() {
            return Truth.assertThat(connection.getPreparedStatementResultSetHandler().getExecutedStatements());
        }

        public @NotNull MockConnectionSubject hasAutocommit(boolean expected) {
            Truth.assertThat(Unchecked.Suppliers.runRethrow(connection::getAutoCommit)).isEqualTo(expected);
            return this;
        }

        public @NotNull MockConnectionSubject wasCommitted(int times) {
            Truth.assertThat(connection.getNumberCommits()).isEqualTo(times);
            return this;
        }

        public @NotNull MockConnectionSubject wasRolledBack(int times) {
            Truth.assertThat(connection.getNumberRollbacks()).isEqualTo(times);
            return this;
        }
    }

    @CanIgnoreReturnValue
    public record MockMockPreparedStatementSubject(@NotNull MockPreparedStatement statement) {
        public @NotNull MockMockPreparedStatementSubject queryEquals(@NotNull String query) {
            Truth.assertThat(statement.getSQL()).isEqualTo(query);
            return this;
        }

        public @NotNull MockMockPreparedStatementSubject hasNoParams() {
            withParams().equalExactly();
            return this;
        }

        public @NotNull MockParameterMapSubject withParams() {
            return new MockParameterMapSubject(statement.getParameterMap());
        }

        public @NotNull MockParameterMapBatchSubject withParamsBatch() {
            List<MockParameterMap> batchParameters = castAny(getPrivateFieldValue(statement, "batchParameters"));
            return new MockParameterMapBatchSubject(batchParameters);
        }

        public @NotNull MockParameterMapSubject withParamsBatchAt(int index) {
            return withParamsBatch().batchAt(index);
        }
    }

    @CanIgnoreReturnValue
    public record MockParameterMapBatchSubject(@NotNull List<MockParameterMap> batch) {
        public void hasSize(int expected) {
            Truth.assertThat(batch.size()).isEqualTo(expected);
        }

        public @NotNull MockParameterMapSubject batchAt(int index) {
            return new MockParameterMapSubject(batch.get(index));
        }
    }

    @CanIgnoreReturnValue
    public record MockParameterMapSubject(@NotNull MockParameterMap parameterMap) {
        public void equalExactly(@Nullable Object @NotNull ... values) {
            for (int i = 0; i < values.length; i++) {
                Truth.assertThat(parameterMap).containsEntry(new ParameterIndex(i + 1), values[i]);
            }
            Truth.assertThat(parameterMap).hasSize(values.length);
        }
    }

    @CanIgnoreReturnValue
    public record MockResultSetSubject(@NotNull MockResultSet resultSet) {
        public void isIdenticalTo(@NotNull MockResultSet expected) {
            hasSameId(expected);
            Truth.assertThat(resultSet.toString()).isEqualTo(expected.toString());
        }

        public void hasSameId(@NotNull MockResultSet expected) {
            Truth.assertThat(resultSet.getId()).isEqualTo(expected.getId());
        }
    }
}
