package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import io.webby.util.base.EasyObjects;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static io.webby.util.base.EasyCast.castAny;

public abstract class Unit implements ArgsHolder {
    private final String repr;
    private final ImmutableList<Object> args;
    private final ImmutableMap<Integer, UnresolvedArg> unresolvedArgs;

    protected Unit(@NotNull String repr) {
        this(repr, ImmutableList.of());
    }

    protected Unit(@NotNull String repr, @NotNull List<?> args) {
        this.repr = repr;
        this.args = unwrapArgs(args);
        this.unresolvedArgs = indexUnresolvedArgs(args);
    }

    @Override
    public @NotNull String repr() {
        return repr;
    }

    @Override
    public @NotNull List<Object> args() {
        return args;
    }

    public @NotNull List<Object> argsAssertingResolved() {
        assert unresolvedArgs.isEmpty() : "Query contains unresolved args: query=`%s` args=`%s` unresolved=`%s`"
            .formatted(this, args, unresolvedArgs);
        return args;
    }

    @Override
    public String toString() {
        return repr;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Unit unit && repr.equals(unit.repr) && args.equals(unit.args) && unresolvedArgs.equals(unit.unresolvedArgs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repr, args, unresolvedArgs);
    }

    private static @NotNull ImmutableList<Object> unwrapArgs(@NotNull List<?> args) {
        return args.stream()
            .map(arg -> arg instanceof UnresolvedArg unresolved ? unresolved.defaultValue() : arg)
            .collect(ImmutableList.toImmutableList());
    }

    private static @NotNull ImmutableMap<Integer, UnresolvedArg> indexUnresolvedArgs(@NotNull List<?> args) {
        Map<Integer, UnresolvedArg> map = null;
        int index = 0;
        for (Object arg : args) {
            ++index;
            if (arg instanceof UnresolvedArg unresolved) {
                map = EasyObjects.firstNonNull(map, HashMap::new);
                map.put(index, unresolved);
            }
        }
        return map != null ? ImmutableMap.copyOf(map) : ImmutableMap.of();
    }

    @SuppressWarnings("UnstableApiUsage")
    protected @NotNull List<Object> resolveArgsByName(@NotNull Map<String, ?> resolved) {
        assert unresolvedArgs.size() == resolved.size() :
            "Provided resolved args size doesn't match unresolved placeholders: " +
            "unresolved=%s, provided=%s".formatted(unresolvedArgs, resolved);
        assert unresolvedArgs.values().stream().map(UnresolvedArg::name).collect(Collectors.toSet()).equals(resolved.keySet()) :
            "Provided resolved args keys don't match unresolved placeholders: " +
            "unresolved=%s, provided=%s".formatted(unresolvedArgs, resolved);
        if (unresolvedArgs.isEmpty()) {
            return args;
        }
        return Streams.mapWithIndex(args.stream(), (arg, index) -> {
            UnresolvedArg unresolvedArg = unresolvedArgs.get((int) index);
            return unresolvedArg != null ? resolved.get(unresolvedArg.name()) : arg;
        }).toList();
    }

    @SuppressWarnings("UnstableApiUsage")
    protected @NotNull List<Object> resolveArgsByOrderedList(@NotNull List<?> resolved) {
        assert unresolvedArgs.size() == resolved.size() :
            "Provided resolved args size doesn't match unresolved placeholders: " +
            "unresolved=%s, provided=%s".formatted(unresolvedArgs, resolved);
        if (unresolvedArgs.size() == args.size()) {
            return castAny(resolved);
        }
        if (unresolvedArgs.isEmpty()) {
            return args;
        }
        Iterator<?> iterator = resolved.iterator();
        return Streams.mapWithIndex(args.stream(), (arg, index) -> {
            UnresolvedArg unresolvedArg = unresolvedArgs.get((int) index);
            return unresolvedArg != null ? iterator.next() : arg;
        }).toList();
    }
}
