package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public record HardcodedStringTerm(@NotNull String repr) implements Term {
    @Override
    public @NotNull TermType type() {
        return TermType.STRING;
    }

    @Override
    public String toString() {
        return repr;
    }
}
