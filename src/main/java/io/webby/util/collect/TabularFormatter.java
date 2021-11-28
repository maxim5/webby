package io.webby.util.collect;

import com.google.common.base.Strings;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record TabularFormatter(@NotNull String cellPrefix,
                               @NotNull String cellDelim,
                               @NotNull String cellSuffix,
                               @NotNull String rowDelim) {
    public static final TabularFormatter FORMATTER = of('|', '-', 1);
    public static final TabularFormatter UTF_FORMATTER = of('\u2502', '\u2014', 1);

    public static @NotNull TabularFormatter of(char columnDelim, char rowDelim, int padding) {
        return new TabularFormatter(
            Strings.padEnd(String.valueOf(columnDelim), padding + 1, ' '),
            Strings.padStart(Strings.padEnd(String.valueOf(columnDelim), padding + 1, ' '), 2 * padding + 1, ' '),
            Strings.padStart(String.valueOf(columnDelim), padding + 1, ' '),
            String.valueOf(rowDelim)
        );
    }

    public <T> @NotNull String formatIntoTableString(@NotNull Tabular<T> tab) {
        if (tab.isEmpty()) {
            return "<empty>";
        }

        int n = tab.rows();
        int m = tab.columns();
        int[] width = new int[m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                T cell = tab.cell(i, j);
                int length = String.valueOf(cell).length();
                width[j] = Math.max(width[j], length);
            }
        }

        String horizontal = Strings.repeat(
            rowDelim,
            Arrays.stream(width).sum() + cellDelim.length() * (m - 1) + cellPrefix.length() + cellSuffix.length()
        );
        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(horizontal);
        for (int i = 0; i < n; i++) {
            List<T> row = tab.rowAt(i);
            formatRow(row, width).forEach(joiner::add);
            joiner.add(horizontal);
        }
        return joiner.toString();
    }

    private <T> @NotNull Stream<String> formatRow(@NotNull List<T> row, int @NotNull [] width) {
        int m = row.size();
        List<String> line = new ArrayList<>(m);
        for (int j = 0; j < m; j++) {
            String cellValue = String.valueOf(row.get(j));
            String cell = Strings.padEnd(cellValue, width[j], ' ');
            line.add(cell);
        }
        return Stream.of(line.stream().collect(Collectors.joining(cellDelim, cellPrefix, cellSuffix)));
    }
}
