package io.spbx.util.testing;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.BitSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.spbx.util.base.EasyCast.castAny;

public class TestingBasics {
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

    public static class SimpleBitSet<T extends SimpleBitSet<T>> {
        protected final BitSet bitSet = new BitSet();

        protected @NotNull T setBit(int bitIndex) {
            bitSet.set(bitIndex);
            return castAny(this);
        }

        protected @NotNull T unsetBit(int bitIndex) {
            bitSet.clear(bitIndex);
            return castAny(this);
        }
    }
}
