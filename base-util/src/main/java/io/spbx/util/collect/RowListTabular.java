package io.spbx.util.collect;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

@Immutable
public class RowListTabular<T, R> implements Tabular<T> {
    private final ImmutableList<R> rows;
    private final Function<R, List<T>> rowToCol;

    public RowListTabular(@NotNull List<R> rows, @NotNull Function<R, List<T>> rowToCol) {
        this.rows = ImmutableList.copyOf(rows);
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
        return rows.isEmpty() ? 0 : rowToCol.apply(rows.getFirst()).size();
    }

    @Override
    public T cell(int row, int col) {
        assert row < rows() && col < columns() : "Out of bounds: [%d, %d]".formatted(row, col);
        return rowToCol.apply(rows.get(row)).get(col);
    }

    @Override
    public @NotNull List<T> rowAt(int row) {
        assert row < rows() : "Out of bounds: [%d]".formatted(row);
        return rowToCol.apply(rows.get(row));
    }

    @Override
    public @NotNull List<T> columnAt(int col) {
        assert col < columns() : "Out of bounds: [%d]".formatted(col);
        return IntStream.range(0, rows()).mapToObj(this::rowAt).map(row -> row.get(col)).toList();
    }
}
