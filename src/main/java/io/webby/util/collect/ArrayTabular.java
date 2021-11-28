package io.webby.util.collect;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class ArrayTabular<T> implements Tabular<T> {
    private final T[][] array;

    public ArrayTabular(T @NotNull[] @NotNull[] array) {
        this.array = array;
    }

    public static <T> @NotNull ArrayTabular<T> of(T @NotNull[] @NotNull... rows) {
        return new ArrayTabular<>(rows);
    }

    @Override
    public int rows() {
        return array.length;
    }

    @Override
    public int columns() {
        return array.length > 0 ? array[0].length : 0;
    }

    @Override
    public T cell(int row, int col) {
        assert row < rows() && col < columns() : "Out of bounds: [%d, %d]".formatted(row, col);
        return array[row][col];
    }

    @Override
    public @NotNull List<T> rowAt(int row) {
        return Arrays.stream(fastRow(row)).toList();
    }

    public T @NotNull[] fastRow(int row) {
        return array[row];
    }

    @Override
    public @NotNull List<T> columnAt(int col) {
        return IntStream.range(0, rows()).mapToObj(row -> array[row][col]).toList();
    }
}
