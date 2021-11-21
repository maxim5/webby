package io.webby.util.sql.api.query;

import org.jetbrains.annotations.NotNull;

public record HardcodedBoolTerm(@NotNull String repr) implements BoolTerm {
    @Override
    public String toString() {
        return repr;
    }
}
