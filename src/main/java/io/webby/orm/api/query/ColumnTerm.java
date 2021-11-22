package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public class ColumnTerm implements Term {
    private final Column column;
    private final String repr;

    public ColumnTerm(@NotNull Column column) {
        this.column = column;
        this.repr = "";
    }

    @Override
    public @NotNull String repr() {
        return repr;
    }

    @Override
    public @NotNull TermType type() {
        return column.type();
    }

    @Override
    public String toString() {
        return repr;
    }
}
