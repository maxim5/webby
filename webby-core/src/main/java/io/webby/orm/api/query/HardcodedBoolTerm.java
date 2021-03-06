package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public record HardcodedBoolTerm(@NotNull String repr) implements BoolTerm {
    @Override
    public @NotNull Args args() {
        return Args.of();
    }

    @Override
    public String toString() {
        return repr;
    }
}
