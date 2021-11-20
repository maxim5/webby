package io.webby.util.sql.arch;

import org.jetbrains.annotations.NotNull;

public record ColumnType(@NotNull JdbcType jdbcType) {
    @Override
    public String toString() {
        return jdbcType.toString();
    }
}
