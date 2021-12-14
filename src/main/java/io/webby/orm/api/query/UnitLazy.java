package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import io.webby.util.lazy.AtomicLazy;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public abstract class UnitLazy implements ArgsHolder {
    private final AtomicLazy<String> repr = new AtomicLazy<>();
    private final ImmutableList<Object> args;

    protected UnitLazy(@NotNull List<Object> args) {
        this.args = ImmutableList.copyOf(args);
    }

    @Override
    public @NotNull String repr() {
        return repr.lazyGet(this::supplyRepr);
    }

    protected abstract @NotNull String supplyRepr();

    @Override
    public @NotNull List<Object> args() {
        return args;
    }

    @Override
    public String toString() {
        return repr();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UnitLazy unit && Objects.equals(repr(), unit.repr()) && Objects.equals(args, unit.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repr(), args);
    }
}
