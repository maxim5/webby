package io.webby.orm.api.query;

import io.webby.util.collect.EasyIterables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Units {
    public static final Collector<CharSequence, ?, String> COMMA_JOINER = Collectors.joining(", ");
    public static final Collector<CharSequence, ?, String> LINE_JOINER = Collectors.joining("\n");

    public static @NotNull String joinWithCommas(@NotNull Collection<? extends Representable> terms) {
        return terms.stream().map(Representable::repr).collect(COMMA_JOINER);
    }

    public static @NotNull String joinWithLines(@Nullable Representable @NotNull ... clauses) {
        return Stream.of(clauses).filter(Objects::nonNull).map(Representable::repr).collect(LINE_JOINER);
    }

    public static @NotNull List<Object> flattenArgsOf(@NotNull ArgsHolder left, @NotNull ArgsHolder right) {
        return EasyIterables.concat(left.args(), right.args());
    }

    public static @NotNull List<Object> flattenArgsOf(@Nullable ArgsHolder @NotNull ... holders) {
        return Arrays.stream(holders).filter(Objects::nonNull).map(ArgsHolder::args).flatMap(Collection::stream).toList();
    }

    public static @NotNull List<Object> flattenArgsOf(@NotNull Collection<? extends ArgsHolder> holders) {
        return holders.stream().filter(Objects::nonNull).map(ArgsHolder::args).flatMap(Collection::stream).toList();
    }
}
