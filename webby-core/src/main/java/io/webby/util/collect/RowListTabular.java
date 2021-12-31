package io.webby.util.collect;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class RowListTabular<T, R> implements Tabular<T> {
    private final List<R> rows;
    private final Function<R, List<T>> rowToCol;

    public RowListTabular(@NotNull List<R> rows, @NotNull Function<R, List<T>> rowToCol) {
        this.rows = rows;
        this.rowToCol = rowToCol;
        for (R row : rows) {
            assert rowToCol.apply(row).size() == columns();
        }
    }

    public static <T> @NotNull RowListTabular<T, List<T>> of(@NotNull List<List<T>> data) {
        return new RowListTabular<>(data, list -> list);
    }

    @Override
    public int rows() {
        return rows.size();
    }

    @Override
    public int columns() {
        return rows.isEmpty() ? 0 : rowToCol.apply(rows.get(0)).size();
    }

    @Override
    public T cell(int row, int col) {
        assert row < rows() && col < columns() : "Out of bounds: [%d, %d]".formatted(row, col);
        return rowToCol.apply(rows.get(row)).get(col);
    }

    @Override
    public @NotNull List<T> rowAt(int row) {
        return rowToCol.apply(rows.get(row));
    }

    @Override
    public @NotNull List<T> columnAt(int col) {
        return IntStream.range(0, rows()).mapToObj(this::rowAt).map(row -> row.get(col)).toList();
    }
}
