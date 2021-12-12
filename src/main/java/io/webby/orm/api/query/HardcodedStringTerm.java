package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record HardcodedStringTerm(@NotNull String repr) implements Term {
    @Override
    public @NotNull TermType type() {
        return TermType.STRING;
    }

    @Override
    public @NotNull List<Object> args() {
        return List.of();
    }

    @Override
    public String toString() {
        return repr;
    }
}
