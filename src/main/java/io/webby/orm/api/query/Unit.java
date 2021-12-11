package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import io.webby.util.collect.EasyIterables;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class Unit implements ArgsHolder {
    private final String repr;
    private final ImmutableList<Object> args;

    protected Unit(@NotNull String repr) {
        this(repr, ImmutableList.of());
    }

    protected Unit(@NotNull String repr, @NotNull List<Object> args) {
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

    protected static @NotNull List<Object> flattenArgsOf(@NotNull ArgsHolder left, @NotNull ArgsHolder right) {
        return EasyIterables.concat(left.args(), right.args());
    }

    protected static @NotNull List<Object> flattenArgsOf(@NotNull Collection<? extends ArgsHolder> holders) {
        return holders.stream().filter(Objects::nonNull).map(ArgsHolder::args).flatMap(Collection::stream).toList();
    }
}
