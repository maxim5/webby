package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record HardcodedTerm(@NotNull String repr, @NotNull TermType type) implements Term {
    @Override
    public @NotNull List<Object> args() {
        return List.of();
    }

    @Override
    public String toString() {
        return repr;
    }
}
