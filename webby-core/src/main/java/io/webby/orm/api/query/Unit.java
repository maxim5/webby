package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public abstract class Unit implements ArgsHolder {
    private final String repr;
    private final ImmutableList<Object> args;

    protected Unit(@NotNull String repr) {
        this(repr, ImmutableList.of());
    }

    protected Unit(@NotNull String repr, @NotNull List<?> args) {
        this.repr = repr;
        this.args = ImmutableList.copyOf(args);
    }

    @Override
    public @NotNull String repr() {
        return repr;
    }

    @Override
    public @NotNull List<Object> args() {
        return args;
    }

    @Override
    public String toString() {
        return repr;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Unit unit && Objects.equals(repr, unit.repr) && Objects.equals(args, unit.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repr, args);
    }
}
