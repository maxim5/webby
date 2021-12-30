package io.webby.util.collect;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Tabular<T> {
    int rows();

    int columns();

    default boolean isEmpty() {
        return rows() == 0 || columns() == 0;
    }

    T cell(int row, int col);

    @NotNull List<T> rowAt(int row);

    @NotNull List<T> columnAt(int col);
}
