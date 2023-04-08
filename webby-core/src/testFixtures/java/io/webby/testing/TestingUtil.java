package io.webby.testing;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestingUtil {
    @SafeVarargs
    public static <T> T[] array(@Nullable T @NotNull ... items) {
        return items;
    }

    @SafeVarargs
    public static <T> @NotNull Iterable<T> iterable(@Nullable T @NotNull ... items) {
        return Stream.of(items)::iterator;
    }

    @SafeVarargs
    public static <T> @NotNull TreeSet<T> sortedSetOf(@Nullable T @NotNull ... items) {
        return Stream.of(items).collect(Collectors.toCollection(TreeSet::new));
    }

    @SafeVarargs
    public static <T> @NotNull T[] appendVarArg(@Nullable T arg, @Nullable T @NotNull ... args) {
        T[] result = Arrays.copyOf(args, args.length + 1);
        result[args.length] = arg;
        return result;
    }

    @SafeVarargs
    public static <T> @NotNull T[] prependVarArg(@Nullable T arg, @Nullable T @NotNull ... args) {
        T[] result = Arrays.copyOf(args, args.length + 1);
        result[0] = arg;
        System.arraycopy(args, 0, result, 1, args.length);
        return result;
    }

    @CanIgnoreReturnValue
    public static boolean waitFor(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException ignore) {
            return false;
        }
    }
}
