package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public class DistinctColumn extends Unit implements Term {
    private final Column column;

    public DistinctColumn(@NotNull Column column) {
        super("DISTINCT %s".formatted(column.repr()));
        this.column = column;
    }

    @Override
    public @NotNull TermType type() {
        return column.type();
    }

    public @NotNull Column column() {
        return column;
    }
}
