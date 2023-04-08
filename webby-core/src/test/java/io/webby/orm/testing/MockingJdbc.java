package io.webby.orm.testing;

import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockParameterMap;
import com.mockrunner.mock.jdbc.MockPreparedStatement;
import com.mockrunner.mock.jdbc.ParameterIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.util.List;

import static io.webby.testing.AssertBasics.getPrivateFieldValue;
import static io.webby.util.base.EasyCast.castAny;

public class MockingJdbc {
    public static @NotNull MockConnection mockConnection() {
        return new MockConnection();
    }

    public static @NotNull MockPreparedStatement mockPreparedStatement() {
        return mockPreparedStatement(null);
    }

    public static @NotNull MockPreparedStatement mockPreparedStatement(@Nullable String query) {
        return new MockPreparedStatement(mockConnection(), query);
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

    public record MockMockPreparedStatementSubject(@NotNull MockPreparedStatement statement) {
        @CanIgnoreReturnValue
        public @NotNull MockMockPreparedStatementSubject queryEquals(@NotNull String query) {
            Truth.assertThat(statement.getSQL()).isEqualTo(query);
            return this;
        }

        @CanIgnoreReturnValue
        public @NotNull MockMockPreparedStatementSubject hasNoParams() {
            withParams().equalExactly();
            return this;
        }

        @CheckReturnValue
        public @NotNull MockParameterMapSubject withParams() {
            return new MockParameterMapSubject(statement.getParameterMap());
        }

        @CheckReturnValue
        public @NotNull MockParameterMapBatchSubject withParamsBatch() {
            List<MockParameterMap> batchParameters = castAny(getPrivateFieldValue(statement, "batchParameters"));
            return new MockParameterMapBatchSubject(batchParameters);
        }

        @CheckReturnValue
        public @NotNull MockParameterMapSubject withParamsBatchAt(int index) {
            return withParamsBatch().batchAt(index);
        }
    }

    public record MockParameterMapBatchSubject(@NotNull List<MockParameterMap> batch) {
        public void hasSize(int expected) {
            Truth.assertThat(batch.size()).isEqualTo(expected);
        }

        @CheckReturnValue
        public @NotNull MockParameterMapSubject batchAt(int index) {
            return new MockParameterMapSubject(batch.get(index));
        }
    }

    public record MockParameterMapSubject(@NotNull MockParameterMap parameterMap) {
        public void equalExactly(@Nullable Object @NotNull ... values) {
            for (int i = 0; i < values.length; i++) {
                Truth.assertThat(parameterMap).containsEntry(new ParameterIndex(i + 1), values[i]);
            }
            Truth.assertThat(parameterMap).hasSize(values.length);
        }
    }
}
