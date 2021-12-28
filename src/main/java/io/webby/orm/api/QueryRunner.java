package io.webby.orm.api;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.LongCursor;
import com.google.common.collect.Lists;
import com.google.errorprone.annotations.MustBeClosed;
import io.webby.orm.api.query.SelectQuery;
import io.webby.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.List;

public class QueryRunner {
    private final Connection connection;

    public QueryRunner(@NotNull Connection connection) {
        this.connection = connection;
    }

    // Run SelectQuery

    public void run(@NotNull SelectQuery query,
                    @NotNull ThrowConsumer<ResultSet, SQLException> resultsConsumer) throws SQLException {
        try (PreparedStatement statement = prepareQuery(query);
             ResultSet resultSet = statement.executeQuery()) {
            resultsConsumer.accept(resultSet);
        }
    }

    public void forEach(@NotNull SelectQuery query,
                        @NotNull ThrowConsumer<ResultSet, SQLException> rowConsumer) throws SQLException {
        run(query, result -> {
           while (result.next()) {
                rowConsumer.accept(result);
            }
        });
    }

    @MustBeClosed
    public <E> @NotNull ResultSetIterator<E> iterate(@NotNull SelectQuery query,
                                                     @NotNull ResultSetIterator.Converter<E> converter) throws SQLException {
        return ResultSetIterator.of(prepareQuery(query).executeQuery(), converter);
    }

    public <E> @NotNull List<E> fetchAll(@NotNull SelectQuery query,
                                         @NotNull ResultSetIterator.Converter<E> converter) throws SQLException {
        try (ResultSetIterator<E> iterator = iterate(query, converter)) {
            return Lists.newArrayList(iterator);
        }
    }

    public @NotNull IntArrayList fetchIntColumn(@NotNull SelectQuery query) throws SQLException {
        IntArrayList list = new IntArrayList();
        forEach(query, resultSet -> list.add(resultSet.getInt(1)));
        return list;
    }

    public @NotNull LongArrayList fetchLongColumn(@NotNull SelectQuery query) throws SQLException {
        LongArrayList list = new LongArrayList();
        forEach(query, resultSet -> list.add(resultSet.getLong(1)));
        return list;
    }

    // Prepare Query

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
                                                   @Nullable Object param1,
                                                   @Nullable Object param2,
                                                   @Nullable Object param3) throws SQLException {
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

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql, @NotNull List<Object> params) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        for (int i = 0; i < params.size(); i++) {
            prepared.setObject(i + 1, params.get(i));
        }
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql, @NotNull IntContainer params) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        int index = 1;
        for (IntCursor cursor : params) {
            prepared.setInt(++index, cursor.value);
        }
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull String sql, @NotNull LongContainer params) throws SQLException {
        PreparedStatement prepared = connection.prepareStatement(sql);
        int index = 1;
        for (LongCursor cursor : params) {
            prepared.setLong(++index, cursor.value);
        }
        return prepared;
    }

    @MustBeClosed
    public @NotNull PreparedStatement prepareQuery(@NotNull SelectQuery query) throws SQLException {
        return prepareQuery(query.repr(), query.args());
    }

    // Run Updates

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
        try (PreparedStatement prepared = prepareQuery(sql, param)) {
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, int param) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql, param)) {
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, long param) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql, param)) {
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, @Nullable Object param1, @Nullable Object param2) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql, param1, param2)) {
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql,
                         @Nullable Object param1,
                         @Nullable Object param2,
                         @Nullable Object param3) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql, param1, param2, param3)) {
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, @Nullable Object @NotNull ... params) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql, params)) {
            return prepared.executeUpdate();
        }
    }

    public int runUpdate(@NotNull String sql, @NotNull List<Object> params) throws SQLException {
        try (PreparedStatement prepared = prepareQuery(sql, params)) {
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
