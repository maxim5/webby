package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record HardcodedBoolTerm(@NotNull String repr) implements BoolTerm {
    @Override
    public @NotNull List<Object> args() {
        return List.of();
    }

    @Override
    public String toString() {
        return repr;
    }
}
