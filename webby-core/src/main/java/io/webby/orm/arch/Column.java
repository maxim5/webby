package io.webby.orm.arch;

import org.jetbrains.annotations.NotNull;

public record Column(@NotNull String sqlName, @NotNull ColumnType type) {
    public @NotNull Column renamed(@NotNull String newName) {
        return new Column(newName, type);
    }

    public @NotNull PrefixedColumn prefixed(@NotNull String prefix) {
        return new PrefixedColumn(this, prefix);
    }

    @Override
    public String toString() {
        return "Column(%s/%s)".formatted(sqlName, type);
    }
}
