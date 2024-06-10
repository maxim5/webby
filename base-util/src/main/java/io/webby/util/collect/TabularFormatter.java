package io.webby.util.collect;

import com.google.common.base.Strings;
import com.google.errorprone.annotations.Immutable;
import org.checkerframework.dataflow.qual.Pure;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Allows to format 2-dimensional table-like data to string. Allows to customize border delimiters. Examples:
 * <p>
 * Alignment:
 * <pre>
 *   -------
 *   | foo |
 *   -------
 *   | 1   |
 *   -------
 * </pre>
 * <p>
 * Multi-lines support:
 * <pre>
 *   -----
 *   | 1 |
 *   | 2 |
 *   -----
 * </pre>
 */
@Immutable
public record TabularFormatter(@NotNull String cellPrefix,
                               @NotNull String cellDelim,
                               @NotNull String cellSuffix,
                               @NotNull String rowDelim) {
    // Uses ASCII characters for boxing.
    public static final TabularFormatter ASCII_FORMATTER = of('|', '-', 1);
    // Uses UTF characters for boxing.
    public static final TabularFormatter UTF_FORMATTER = of('\u2502', '\u2014', 1);

    public static @NotNull TabularFormatter of(char columnDelim, char rowDelim, int padding) {
        return new TabularFormatter(
            Strings.padEnd(String.valueOf(columnDelim), padding + 1, ' '),
            Strings.padStart(Strings.padEnd(String.valueOf(columnDelim), padding + 1, ' '), 2 * padding + 1, ' '),
            Strings.padStart(String.valueOf(columnDelim), padding + 1, ' '),
            String.valueOf(rowDelim)
        );
    }

    @Pure
    public <T> @NotNull String formatIntoTableString(@NotNull Tabular<T> tab) {
        if (tab.isEmpty()) {
            return "<empty>";
        }

        int n = tab.rows();
        int m = tab.columns();
        int[] width = new int[m];
        int[] height = new int[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                T cell = tab.cell(i, j);
                int lineWidth = String.valueOf(cell).lines().mapToInt(String::length).max().orElse(0);
                int lineHeight = Math.max((int) String.valueOf(cell).lines().count(), 1);
                width[j] = Math.max(width[j], lineWidth);
                height[i] = Math.max(height[i], lineHeight);
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
            formatRow(row, width, height[i]).forEach(joiner::add);
            joiner.add(horizontal);
        }
        return joiner.toString();
    }

    private <T> @NotNull Stream<String> formatRow(@NotNull List<T> row, int @NotNull [] width, int height) {
        int m = row.size();
        String[][] pieces = new String[height][m];
        for (int j = 0; j < m; j++) {
            String cellValue = String.valueOf(row.get(j));
            List<String> cellLines = cellValue.lines().toList();
            for (int i = 0; i < height; i++) {
                pieces[i][j] = Strings.padEnd(i < cellLines.size() ? cellLines.get(i) : "", width[j], ' ');
            }
        }
        return Arrays.stream(pieces)
                .map(line -> Arrays.stream(line).collect(Collectors.joining(cellDelim, cellPrefix, cellSuffix)));
    }
}
