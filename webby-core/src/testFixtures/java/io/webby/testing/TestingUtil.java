package io.webby.testing;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class TestingUtil {
    @SafeVarargs
    public static <T> T[] array(@Nullable T @NotNull... items) {
        return items;
    }

    @SafeVarargs
    public static <T> @NotNull T[] appendVarArg(@Nullable T arg, @Nullable T @NotNull ... args) {
        T[] result = Arrays.copyOf(args, args.length + 1);
        result[args.length] = arg;
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
