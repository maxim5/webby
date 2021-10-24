package io.webby.util.sql.api;

import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class QueryRunner {
    private final Connection connection;

    public QueryRunner(@NotNull Connection connection) {
        this.connection = connection;
    }

    // Query

    public @NotNull ResultSet runQuery(@NotNull String sql) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    public @NotNull ResultSet runQuery(@NotNull String sql, @NotNull Object param) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setObject(1, param);
        return prepared.executeQuery();
    }

    public @NotNull ResultSet runQuery(@NotNull String sql, int param) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setInt(1, param);
        return prepared.executeQuery();
    }

    public @NotNull ResultSet runQuery(@NotNull String sql, long param) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setLong(1, param);
        return prepared.executeQuery();
    }

    public @NotNull ResultSet runQuery(@NotNull String sql, @NotNull Object param1, @NotNull Object param2) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setObject(1, param1);
        prepared.setObject(2, param2);
        return prepared.executeQuery();
    }

    public @NotNull ResultSet runQuery(@NotNull String sql,
                                       @NotNull Object param1,
                                       @NotNull Object param2,
                                       @NotNull Object param3) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setObject(1, param1);
        prepared.setObject(2, param2);
        prepared.setObject(3, param3);
        return prepared.executeQuery();
    }

    public @NotNull ResultSet runQuery(@NotNull String sql, @NotNull Object @NotNull ... params) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            prepared.setObject(i + 1, params[i]);
        }
        return prepared.executeQuery();
    }

    // Updates

    public int runUpdate(@NotNull String sql) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeUpdate(sql);
    }

    public int runUpdate(@NotNull String sql, @NotNull Object param) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setObject(1, param);
        return prepared.executeUpdate();
    }

    public int runUpdate(@NotNull String sql, int param) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setInt(1, param);
        return prepared.executeUpdate();
    }

    public int runUpdate(@NotNull String sql, long param) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setLong(1, param);
        return prepared.executeUpdate();
    }

    public int runUpdate(@NotNull String sql, @NotNull Object param1, @NotNull Object param2) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setObject(1, param1);
        prepared.setObject(2, param2);
        return prepared.executeUpdate();
    }

    public int runUpdate(@NotNull String sql,
                         @NotNull Object param1,
                         @NotNull Object param2,
                         @NotNull Object param3) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        prepared.setObject(1, param1);
        prepared.setObject(2, param2);
        prepared.setObject(3, param3);
        return prepared.executeUpdate();
    }

    public int runUpdate(@NotNull String sql, @NotNull Object @NotNull ... params) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            prepared.setObject(i + 1, params[i]);
        }
        return prepared.executeUpdate();
    }
}
