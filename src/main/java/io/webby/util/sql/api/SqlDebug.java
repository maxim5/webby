package io.webby.util.sql.api;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlDebug {
    public static @NotNull ResultSetIterator<Row> iterateRows(@NotNull ResultSet resultSet) {
        return new ResultSetIterator<>(resultSet, SqlDebug::toDebugRow);
    }

    public static @NotNull List<Row> toDebugRows(@NotNull ResultSet resultSet) {
        try (ResultSetIterator<Row> iterator = iterateRows(resultSet)) {
            return Lists.newArrayList(iterator);
        }
    }

    public static @NotNull Row getSingleDebugRowOrDie(@NotNull ResultSet resultSet) {
        List<Row> rows = toDebugRows(resultSet);
        assert rows.size() == 1 : "Expected exactly one row, found: " + rows;
        return rows.get(0);
    }

    public static @NotNull Row toDebugRow(@NotNull ResultSet row) throws SQLException {
        ResultSetMetaData metaData = row.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<RowValue> values = new ArrayList<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            values.add(new RowValue(metaData.getColumnName(i), row.getString(i)));
        }
        return new Row(values);
    }

    public record Row(@NotNull List<RowValue> values) {
        public @NotNull Optional<RowValue> findValue(@NotNull String name) {
            return values.stream().filter(val -> val.name.equals(name)).findFirst();
        }
    }

    public record RowValue(@NotNull String name, @NotNull String value) {}
}
