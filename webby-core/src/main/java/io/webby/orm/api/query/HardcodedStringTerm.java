package io.webby.orm.api.query;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

@Immutable
public record HardcodedStringTerm(@NotNull String repr) implements Term {
    @Override
    public @NotNull TermType type() {
        return TermType.STRING;
    }

    @Override
    public @NotNull Args args() {
        return Args.of();
    }

    @Override
    public String toString() {
        return repr;
    }
}
