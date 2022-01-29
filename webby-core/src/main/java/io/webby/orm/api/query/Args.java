package io.webby.orm.api.query;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.LongArrayList;
import com.carrotsearch.hppc.LongContainer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.webby.orm.api.QueryRunner;
import io.webby.util.base.EasyObjects;
import io.webby.util.hppc.EasyHppc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Args {
    private final InternalType type;
    private final Iterable<?> internal;  // original set of args, may contain unresolved
    private final Iterable<?> external;  // args for external use, all unresolved replaced with default values

    private Args(@NotNull InternalType type, @NotNull Iterable<?> internal) {
        this.type = type;
        this.internal = internal;
        this.external = type == InternalType.GENERIC_LIST ? unwrapArgs((List<?>) internal) : internal;
    }

    private Args(@NotNull InternalType type, @NotNull Iterable<?> internal, @NotNull Iterable<?> external) {
        this.type = type;
        this.internal = internal;
        this.external = external;
    }

    public static @NotNull Args of() {
        return new Args(InternalType.GENERIC_LIST, ImmutableList.of());
    }

    public static @NotNull Args of(@Nullable Object @NotNull ... args) {
        return new Args(InternalType.GENERIC_LIST, ImmutableList.copyOf(args));
    }

    public static @NotNull Args of(@NotNull Iterable<?> args) {
        return new Args(InternalType.GENERIC_LIST, ImmutableList.copyOf(args));
    }

    public static @NotNull Args of(int arg) {
        return new Args(InternalType.INTS, IntArrayList.from(arg));
    }

    public static @NotNull Args of(int arg1, int arg2) {
        return new Args(InternalType.INTS, IntArrayList.from(arg1, arg2));
    }

    public static @NotNull Args of(int ... args) {
        return new Args(InternalType.INTS, IntArrayList.from(args));
    }

    public static @NotNull Args of(@NotNull IntContainer args) {
        return new Args(InternalType.INTS, args);
    }

    public static @NotNull Args of(long arg) {
        return new Args(InternalType.LONGS, LongArrayList.from(arg));
    }

    public static @NotNull Args of(long arg1, long arg2) {
        return new Args(InternalType.LONGS, LongArrayList.from(arg1, arg2));
    }

    public static @NotNull Args of(long ... args) {
        return new Args(InternalType.LONGS, LongArrayList.from(args));
    }

    public static @NotNull Args of(@NotNull LongContainer args) {
        return new Args(InternalType.LONGS, args);
    }

    public static @NotNull Args concat(@NotNull Args lhs, @NotNull Args rhs) {
        // FIX[minor]: optimize for ints and longs
        return of(new ImmutableList.Builder<>().addAll(lhs.internalList()).addAll(rhs.internalList()).build());
    }

    public static @NotNull Args flattenArgsOf(@NotNull HasArgs left, @NotNull HasArgs right) {
        return concat(left.args(), right.args());
    }

    public static @NotNull Args flattenArgsOf(@Nullable HasArgs @NotNull ... items) {
        return of(Arrays.stream(items)
                      .filter(Objects::nonNull)
                      .map(HasArgs::args)
                      .flatMap(Args::internalStream)
                      .collect(ImmutableList.toImmutableList()));
    }

    public static @NotNull Args flattenArgsOf(@NotNull Collection<? extends HasArgs> items) {
        return of(items.stream()
                      .filter(Objects::nonNull)
                      .map(HasArgs::args)
                      .flatMap(Args::internalStream)
                      .collect(ImmutableList.toImmutableList()));
    }

    private static @NotNull Args resolved(@NotNull Iterable<?> args) {
        // FIX[minor]: assert all resolved?
        return new Args(InternalType.GENERIC_LIST, ImmutableList.copyOf(args), ImmutableList.copyOf(args));
    }

    public boolean isEmpty() {
        return switch (type) {
            case GENERIC_LIST -> ((List<?>) external).isEmpty();
            case INTS -> ((IntContainer) external).isEmpty();
            case LONGS -> ((LongContainer) external).isEmpty();
        };
    }

    public int size() {
        return switch (type) {
            case GENERIC_LIST -> ((List<?>) external).size();
            case INTS -> ((IntContainer) external).size();
            case LONGS -> ((LongContainer) external).size();
        };
    }

    public @NotNull List<?> asList() {
        return switch (type) {
            case GENERIC_LIST -> (List<?>) external;
            case INTS -> EasyHppc.toJavaList((IntContainer) external);
            case LONGS -> EasyHppc.toJavaList((LongContainer) external);
        };
    }

    public boolean isAllResolved() {
        return type != InternalType.GENERIC_LIST || ((List<?>) internal).stream().noneMatch(arg -> arg instanceof UnresolvedArg);
    }

    public void setPreparedParams(@NotNull PreparedStatement statement) throws SQLException {
        setPreparedParams(statement, 0);
    }

    public void setPreparedParams(@NotNull PreparedStatement statement, int index) throws SQLException {
        switch (type) {
            case GENERIC_LIST -> QueryRunner.setPreparedParams(statement, external, index);
            case INTS -> QueryRunner.setPreparedParams(statement, (IntContainer) external, index);
            case LONGS -> QueryRunner.setPreparedParams(statement, (LongContainer) external, index);
        }
    }

    private @NotNull List<?> internalList() {
        return switch (type) {
            case GENERIC_LIST -> (List<?>) internal;
            case INTS -> EasyHppc.toJavaList((IntContainer) internal);
            case LONGS -> EasyHppc.toJavaList((LongContainer) internal);
        };
    }

    private @NotNull Stream<?> internalStream() {
        return internalList().stream();
    }

    private static @NotNull ImmutableList<Object> unwrapArgs(@NotNull List<?> args) {
        return args.stream()
            .map(arg -> arg instanceof UnresolvedArg unresolved ? unresolved.defaultValue() : arg)
            .collect(ImmutableList.toImmutableList());
    }

    // FIX[minor]: Use array or IntObjMap?
    private @NotNull Map<Integer, UnresolvedArg> unresolvedArgs() {
        if (type != InternalType.GENERIC_LIST) {
            return Map.of();
        }
        Map<Integer, UnresolvedArg> map = null;
        int index = 0;
        for (Object arg : internal) {
            ++index;
            if (arg instanceof UnresolvedArg unresolved) {
                map = EasyObjects.firstNonNull(map, HashMap::new);
                map.put(index, unresolved);
            }
        }
        return map != null ? ImmutableMap.copyOf(map) : ImmutableMap.of();
    }

    /*package*/ @NotNull Args resolveArgsByName(@NotNull Map<String, ?> resolved) {
        Map<Integer, UnresolvedArg> unresolvedArgs = unresolvedArgs();
        assert unresolvedArgs.size() == resolved.size() :
            "Provided resolved args size doesn't match unresolved placeholders: " +
            "internal=%s, provided=%s".formatted(internal, resolved);
        assert unresolvedArgs.values().stream().map(UnresolvedArg::name).collect(Collectors.toSet()).equals(resolved.keySet()) :
            "Provided resolved args keys don't match unresolved placeholders: " +
            "internal=%s, provided=%s".formatted(internal, resolved);

        if (unresolvedArgs.isEmpty()) {
            return this;
        }

        assert type == InternalType.GENERIC_LIST : "Internal error: type=%s, internal=%s".formatted(type, internal);

        ImmutableList.Builder<Object> result = ImmutableList.builder();
        int index = 0;
        for (Object arg : internal) {
            UnresolvedArg unresolvedArg = unresolvedArgs.get(++index);
            result.add(unresolvedArg != null ? resolved.get(unresolvedArg.name()) : arg);
        }
        return Args.resolved(result.build());
    }

    /*package*/ @NotNull Args resolveArgsByOrderedList(@NotNull List<?> resolved) {
        Map<Integer, UnresolvedArg> unresolvedArgs = unresolvedArgs();
        assert unresolvedArgs.size() == resolved.size() :
            "Provided resolved args size doesn't match unresolved placeholders: " +
            "internal=%s, provided=%s".formatted(internal, resolved);

        if (type == InternalType.GENERIC_LIST && unresolvedArgs.size() == ((List<?>) internal).size()) {
            return Args.resolved(resolved);
        }
        if (unresolvedArgs.isEmpty()) {
            return this;
        }

        assert type == InternalType.GENERIC_LIST : "Internal error: type=%s, internal=%s".formatted(type, internal);

        ImmutableList.Builder<Object> result = ImmutableList.builder();
        Iterator<?> iterator = resolved.iterator();
        int index = 0;
        for (Object arg : internal) {
            UnresolvedArg unresolvedArg = unresolvedArgs.get(++index);
            result.add(unresolvedArg != null ? iterator.next() : arg);
        }
        return Args.resolved(result.build());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Args args && type == args.type && internal.equals(args.internal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, internal);
    }

    @Override
    public String toString() {
        return internal.toString();
    }

    private enum InternalType {
        GENERIC_LIST,
        INTS,
        LONGS,
    }
}
