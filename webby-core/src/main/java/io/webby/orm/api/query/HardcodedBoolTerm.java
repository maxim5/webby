package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public class HardcodedBoolTerm extends Unit implements BoolTerm {
    public HardcodedBoolTerm(@NotNull String repr, @NotNull Args args) {
        super(repr, args);
    }

    public HardcodedBoolTerm(@NotNull String repr) {
        super(repr);
    }
}
