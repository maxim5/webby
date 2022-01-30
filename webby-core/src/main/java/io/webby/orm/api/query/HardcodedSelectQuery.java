package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public class HardcodedSelectQuery extends Unit implements SelectQuery {
    public HardcodedSelectQuery(@NotNull String repr, @NotNull Args args) {
        super(repr, args);
    }

    public static @NotNull HardcodedSelectQuery of(@NotNull String query, @NotNull Args args) {
        return new HardcodedSelectQuery(query, args);
    }

    public static @NotNull HardcodedSelectQuery of(@NotNull String query) {
        return of(query, Args.of());
    }

    @Override
    public int columnsNumber() {
        return -1;
    }
}
