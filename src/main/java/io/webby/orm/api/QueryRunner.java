package io.webby.orm.api;

import com.google.errorprone.annotations.MustBeClosed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;

public class QueryRunner {
    private final Connection connection;

    public QueryRunner(@NotNull Connection connection) {
        this.connection = connection;
    }

    // Query

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql, @Nullable Object param) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setObject(1, param);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql, int param) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setInt(1, param);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql, long param) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setLong(1, param);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql,
                                                   @Nullable Object param1,
                                                   @Nullable Object param2) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setObject(1, param1);
        prepared.setObject(2, param2);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql,
                                                   @NotNull Object param1,
                                                   @NotNull Object param2,
                                                   @NotNull Object param3) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setObject(1, param1);
        prepared.setObject(2, param2);
        prepared.setObject(3, param3);
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql,
                                                   @Nullable Object @NotNull ... params) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            prepared.setObject(i + 1, params[i]);
        }
        return prepared;
    }

    // Updates

    public int runMultiUpdate(@NotNull String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(sql);
        }
    }

    public int runUpdate(@NotNull String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            return statement.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, @Nullable Object param) throws SQLException {
        try (PreparedStatement prepared = connection.prepareStatement(sql)) {
            prepared.setObject(1, param);
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, int param) throws SQLException {
        try (PreparedStatement prepared = connection.prepareStatement(sql)) {
            prepared.setInt(1, param);
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, long param) throws SQLException {
        try (PreparedStatement prepared = connection.prepareStatement(sql)) {
            prepared.setLong(1, param);
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, @Nullable Object param1, @Nullable Object param2) throws SQLException {
        try (PreparedStatement prepared = connection.prepareStatement(sql)) {
            prepared.setObject(1, param1);
            prepared.setObject(2, param2);
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql,
                         @Nullable Object param1,
                         @Nullable Object param2,
                         @Nullable Object param3) throws SQLException {
        try (PreparedStatement prepared = connection.prepareStatement(sql)) {
            prepared.setObject(1, param1);
            prepared.setObject(2, param2);
            prepared.setObject(3, param3);
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, @Nullable Object @NotNull ... params) throws SQLException {
        try (PreparedStatement prepared = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                prepared.setObject(i + 1, params[i]);
            }
            return prepared.executeUpdate();
        }
    }

    public @NotNull AutoIncResult runAutoIncUpdate(@NotNull String sql,
                                                   @Nullable Object @NotNull ... params) throws SQLException {
        try (PreparedStatement prepared = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.length; i++) {
                prepared.setObject(i + 1, params[i]);
            }

            // See https://stackoverflow.com/questions/1915166/how-to-get-the-insert-id-in-jdbc
            int changedRowCount = prepared.executeUpdate();
            try (ResultSet generatedKeys = prepared.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new AutoIncResult(changedRowCount, generatedKeys.getLong(1));
                } else {
                    throw new QueryException("No generated keys returned. Changed row count=%d".formatted(changedRowCount), sql, params);
                }
            }
        }
    }

    public record AutoIncResult(int changedRowCount, long lastId) {
    }
}
