package io.webby.util.sql.api;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SqlDebug {
    public static final SqlTableFormatter FORMATTER = new SqlTableFormatter(" ", " | ", " ", true, '-');

    public static @NotNull ResultSetIterator<Row> iterateRows(@NotNull ResultSet resultSet) {
        return new ResultSetIterator<>(resultSet, SqlDebug::toDebugRow);
    }

    public static @NotNull List<Row> toDebugRows(@NotNull ResultSet resultSet) {
        try (ResultSetIterator<Row> iterator = iterateRows(resultSet)) {
            return Lists.newArrayList(iterator);
        }
    }

    public static @NotNull String toDebugString(@NotNull ResultSet resultSet) {
        return FORMATTER.formatIntoTableString(toDebugRows(resultSet));
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

    public record SqlTableFormatter(@NotNull String prefix,
                                    @NotNull String delim,
                                    @NotNull String suffix,
                                    boolean withHeader,
                                    char headerSeparator) {
        public @NotNull String formatIntoTableString(@NotNull List<Row> rows) {
            if (rows.isEmpty()) {
                return "<empty>";
            }

            int n = rows.size();
            int m = rows.get(0).values().size();
            int[] width = new int[m];
            for (Row row : rows) {
                List<RowValue> values = row.values();
                for (int j = 0; j < m; j++) {
                    RowValue rowValue = values.get(j);
                    String name = rowValue.name();
                    String val = rowValue.value();
                    int length = withHeader ? Math.max(val.length(), name.length()) : val.length();
                    width[j] = Math.max(width[j], length);
                }
            }

            List<String> lines = new ArrayList<>(n);
            if (withHeader) {
                lines.add(formatLine(rows.get(0), RowValue::name, width));
                lines.add(Strings.repeat(String.valueOf(headerSeparator),
                                         Arrays.stream(width).sum() + delim.length() * (m - 1) +
                                         prefix.length() + suffix.length()));
            }
            for (Row row : rows) {
                lines.add(formatLine(row, RowValue::value, width));
            }
            return String.join("\n", lines);
        }

        @NotNull
        private String formatLine(@NotNull Row row, @NotNull Function<RowValue, String> valueCapture, int @NotNull [] width) {
            List<RowValue> values = row.values();
            int m = values.size();
            List<String> line = new ArrayList<>(m);
            for (int j = 0; j < m; j++) {
                String cellValue = valueCapture.apply(values.get(j));
                String cell = Strings.padEnd(cellValue, width[j], ' ');
                line.add(cell);
            }
            return line.stream().collect(Collectors.joining(delim, prefix, suffix));
        }
    }

    public record Row(@NotNull List<RowValue> values) {
        public @NotNull Optional<RowValue> findValue(@NotNull String name) {
            return values.stream().filter(val -> val.name.equals(name)).findFirst();
        }
    }

    public record RowValue(@NotNull String name, @NotNull String value) {}
}
