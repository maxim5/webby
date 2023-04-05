package io.webby.util.collect;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents 2-dimensional table-like structure, for example 2d array or list of lists.
 *
 * @param <T> type of items
 */
public interface Tabular<T> {
    /**
     * Returns the number of rows. Can be 0, but not negative.
     */
    int rows();

    /**
     * Returns the number of columns. Can be 0, but not negative.
     */
    int columns();

    /**
     * Returns true iff the tabular is empty.
     */
    default boolean isEmpty() {
        return rows() == 0 || columns() == 0;
    }

    /**
     * Returns the raw value at the specified cell.
     */
    T cell(int row, int col);

    /**
     * Returns the column values at the particular row.
     */
    @NotNull List<T> rowAt(int row);

    /**
     * Returns the row values at the particular column.
     */
    @NotNull List<T> columnAt(int col);
}
