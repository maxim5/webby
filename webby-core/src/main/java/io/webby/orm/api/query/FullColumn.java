package io.webby.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public class FullColumn extends Unit implements Column {
    private final Column column;

    public FullColumn(@NotNull Column column, @NotNull String table) {
        super("%s.%s".formatted(table, column.name()));
        this.column = column;
    }

    @Override
    public @NotNull TermType type() {
        return column.type();
    }

    @Override
    public @NotNull String name() {
        return column.name();
    }
}
