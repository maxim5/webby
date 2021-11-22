package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public enum CompareType implements Repr {
    EQ("="),
    NE("<>"),
    GT(">"),
    LT("<"),
    GE(">="),
    LE("<=");

    private final String repr;

    CompareType(@NotNull String repr) {
        this.repr = repr;
    }

    @Override
    public @NotNull String repr() {
        return repr;
    }

    public @NotNull Compare compare(@NotNull Term lhs, @NotNull Term rhs) {
        return new Compare(lhs, rhs, this);
    }
}
