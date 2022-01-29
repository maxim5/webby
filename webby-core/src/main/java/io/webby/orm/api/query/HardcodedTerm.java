package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public record HardcodedTerm(@NotNull String repr, @NotNull TermType type) implements Term {
    @Override
    public @NotNull Args args() {
        return Args.of();
    }

    @Override
    public String toString() {
        return repr;
    }
}
