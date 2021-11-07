package io.webby.util.sql.api;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TableMeta {
    @NotNull String sqlTableName();

    @NotNull List<String> sqlColumns();
}
