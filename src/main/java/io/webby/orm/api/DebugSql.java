package io.webby.orm.api;

import com.google.common.collect.Lists;
import io.webby.util.collect.ArrayTabular;
import io.webby.util.collect.Tabular;
import io.webby.util.collect.TabularFormatter;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class DebugSql {
    public static @NotNull ResultSetIterator<Row> iterateRows(@NotNull ResultSet resultSet) {
        return ResultSetIterator.of(resultSet, DebugSql::toDebugRow);
    }

    public static @NotNull List<Row> toDebugRows(@NotNull ResultSet resultSet) {
        try (ResultSetIterator<Row> iterator = iterateRows(resultSet)) {
            return Lists.newArrayList(iterator);
        }
    }

    public static @NotNull String toDebugString(@NotNull ResultSet resultSet) {
        return toDebugString(toDebugRows(resultSet));
    }

    public static @NotNull String toDebugString(@NotNull List<Row> rows) {
        return TabularFormatter.FORMATTER.formatIntoTableString(Row.toTabular(rows, true));
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
            values.add(new RowValue(metaData.getColumnName(i), String.valueOf(row.getString(i))));
        }
        return new Row(values);
    }

    public record Row(@NotNull List<RowValue> values) {
        public @NotNull Optional<RowValue> findValue(@NotNull String name) {
            return values.stream().filter(val -> val.name.equals(name)).findFirst();
        }

        public @NotNull RowValue getValueAt(int columnIndex) {
            return values.get(columnIndex);
        }

        public static @NotNull Tabular<String> toTabular(@NotNull List<Row> rows, boolean withHeader) {
            if (rows.isEmpty()) {
                return ArrayTabular.of(new String[0][0]);
            }
            int shift = withHeader ? 1 : 0;
            String[][] array = new String[rows.size() + shift][];
            array[0] = withHeader ? rows.get(0).toStringArray(RowValue::name) : null;
            for (int i = 0; i < rows.size(); i++) {
                array[i + shift] = rows.get(i).toStringArray(RowValue::value);
            }
            return ArrayTabular.of(array);
        }

        private @NotNull String[] toStringArray(@NotNull Function<RowValue, String> mapper) {
            return values.stream().map(mapper).toArray(String[]::new);
        }
    }

    public record RowValue(@NotNull String name, @NotNull String value) {}
}
