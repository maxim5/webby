package io.webby.orm.api.debug;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.orm.api.*;
import io.webby.orm.api.query.SelectQuery;
import io.webby.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface DebugRunner extends WithRunner {
    default @NotNull List<DebugSql.Row> runQuery(@NotNull String query, @Nullable Object @NotNull ... params) {
        try (PreparedStatement statement = runner().prepareQuery(query, params);
             ResultSet resultSet = statement.executeQuery()) {
            return DebugSql.toDebugRows(resultSet);
        } catch (SQLException e) {
            return Unchecked.rethrow(e);
        }
    }

    default @NotNull List<DebugSql.Row> runQuery(@NotNull SelectQuery query) {
        return runQuery(query.repr(), query.args().toArray());
    }

    default @NotNull String runQueryToString(@NotNull String query, @Nullable Object @NotNull ... params) {
        return DebugSql.toDebugString(runQuery(query, params));
    }

    default @NotNull String runQueryToString(@NotNull SelectQuery query) {
        return runQueryToString(query.repr(), query.args().toArray());
    }

    @CanIgnoreReturnValue
    default int runUpdate(@NotNull String sql, @Nullable Object @NotNull ... params) {
        try {
            return runner().runUpdate(sql, params);
        } catch (SQLException e) {
            return Unchecked.rethrow(e);
        }
    }

    static @NotNull DebugRunner of(@NotNull Connector connector) {
        return connector::runner;
    }

    static @NotNull DebugRunner of(@NotNull Connection connection) {
        return () -> new QueryRunner(connection);
    }

    static @NotNull DebugRunner of(@NotNull QueryRunner runner) {
        return () -> runner;
    }

    static @NotNull DebugRunner of(@NotNull BaseTable<?> table) {
        return of(table.runner());
    }
}
