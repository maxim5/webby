package io.webby.orm.api;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DebugSql {
    public static final SqlTableFormatter FORMATTER = SqlTableFormatter.of(true, '|', '-', 1);
    public static final SqlTableFormatter UTF_FORMATTER = SqlTableFormatter.of(true, '\u2502', '\u2014', 1);

    public static @NotNull ResultSetIterator<Row> iterateRows(@NotNull ResultSet resultSet) {
        return ResultSetIterator.of(resultSet, DebugSql::toDebugRow);
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
            values.add(new RowValue(metaData.getColumnName(i), String.valueOf(row.getString(i))));
        }
        return new Row(values);
    }

    public record SqlTableFormatter(boolean withHeader,
                                    @NotNull String cellPrefix,
                                    @NotNull String cellDelim,
                                    @NotNull String cellSuffix,
                                    @NotNull String rowDelim) {
        public static @NotNull SqlTableFormatter of(boolean withHeader, char columnDelim, char rowDelim, int padding) {
            return new SqlTableFormatter(
                withHeader,
                Strings.padEnd(String.valueOf(columnDelim), padding + 1, ' '),
                Strings.padStart(Strings.padEnd(String.valueOf(columnDelim), padding + 1, ' '), 2 * padding + 1, ' '),
                Strings.padStart(String.valueOf(columnDelim), padding + 1, ' '),
                String.valueOf(rowDelim)
            );
        }

        public @NotNull String formatIntoTableString(@NotNull List<Row> rows) {
            if (rows.isEmpty()) {
                return "<empty>";
            }

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

            String horizontal = Strings.repeat(
                rowDelim,
                Arrays.stream(width).sum() + cellDelim.length() * (m - 1) + cellPrefix.length() + cellSuffix.length()
            );
            StringJoiner joiner = new StringJoiner("\n");
            joiner.add(horizontal);
            if (withHeader) {
                formatRow(rows.get(0), RowValue::name, width).forEach(joiner::add);
                joiner.add(horizontal);
            }
            for (Row row : rows) {
                formatRow(row, RowValue::value, width).forEach(joiner::add);
                joiner.add(horizontal);
            }
            return joiner.toString();
        }

        private @NotNull Stream<String> formatRow(@NotNull Row row,
                                                  @NotNull Function<RowValue, String> valueCapture,
                                                  int @NotNull [] width) {
            List<RowValue> values = row.values();
            int m = values.size();
            List<String> line = new ArrayList<>(m);
            for (int j = 0; j < m; j++) {
                String cellValue = valueCapture.apply(values.get(j));
                String cell = Strings.padEnd(cellValue, width[j], ' ');
                line.add(cell);
            }
            return Stream.of(line.stream().collect(Collectors.joining(cellDelim, cellPrefix, cellSuffix)));
        }
    }

    public record Row(@NotNull List<RowValue> values) {
        public @NotNull Optional<RowValue> findValue(@NotNull String name) {
            return values.stream().filter(val -> val.name.equals(name)).findFirst();
        }
    }

    public record RowValue(@NotNull String name, @NotNull String value) {}

    public static class DebugRunner extends QueryRunner {
        public DebugRunner(@NotNull Connection connection) {
            super(connection);
        }

        public void update(@NotNull String query, @Nullable Object @NotNull ... params) throws SQLException {
            System.out.println(">>> " + query.trim());
            System.out.println(runUpdate(query, params));
            System.out.println();
        }

        public void query(@NotNull String query, @Nullable Object @NotNull ... params) throws SQLException {
            System.out.println(">>> " + query.trim());
            try (PreparedStatement statement = prepareQuery(query, params);
                 ResultSet result = statement.executeQuery()) {
                System.out.println(DebugSql.toDebugString(result));
            }
            System.out.println();
        }
    }
}
