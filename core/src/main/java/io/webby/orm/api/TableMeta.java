package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TableMeta {
    @NotNull String sqlTableName();

    @NotNull List<ColumnMeta> sqlColumns();

    record ColumnMeta(@NotNull String name, @NotNull Class<?> type, boolean isPrimaryKey, boolean isForeignKey) {}
}
