package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Unit implements HasArgs {
    private final String repr;
    private final Args args;

    protected Unit(@NotNull String repr) {
        this(repr, Args.of());
    }

    protected Unit(@NotNull String repr, @NotNull Args args) {
        this.repr = repr;
        this.args = args;
    }

    @Override
    public @NotNull String repr() {
        return repr;
    }

    @Override
    public @NotNull Args args() {
        return args;
    }

    @Override
    public String toString() {
        return repr;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Unit unit && repr.equals(unit.repr) && args.equals(unit.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repr, args);
    }
}
