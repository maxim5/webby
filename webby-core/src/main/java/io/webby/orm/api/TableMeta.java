package io.webby.orm.api;

import io.webby.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Holds the meta information of the SQL table.
 */
public interface TableMeta {
    /**
     * Returns the table name in SQL.
     */
    @NotNull String sqlTableName();

    /**
     * Returns the meta info for all columns in the table.
     */
    @NotNull List<ColumnMeta> sqlColumns();

    /**
     * Holds the meta information of the SQL table.
     */
    record ColumnMeta(@NotNull Column column, @NotNull Class<?> type, boolean isPrimaryKey, boolean isForeignKey) {
        public @NotNull String name() {
            return column.name();
        }
    }
}
