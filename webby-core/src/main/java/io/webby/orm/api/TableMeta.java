package io.webby.orm.api;

import io.webby.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TableMeta {
    @NotNull String sqlTableName();

    @NotNull List<ColumnMeta> sqlColumns();

    record ColumnMeta(@NotNull Column column, @NotNull Class<?> type, boolean isPrimaryKey, boolean isForeignKey) {
        public @NotNull String name() {
            return column.name();
        }
    }
}
