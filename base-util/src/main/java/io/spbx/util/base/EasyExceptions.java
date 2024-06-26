package io.spbx.util.base;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EasyExceptions {
    public static class IllegalArgumentExceptions {
        public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
            if (!cond) {
                IllegalArgumentExceptions.fail(message, args);
            }
        }

        public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
            if (cond) {
                IllegalArgumentExceptions.fail(message, args);
            }
        }

        @CanIgnoreReturnValue
        public static <R> R fail(@NotNull String message, @Nullable Object @NotNull ... args) {
            throw IllegalArgumentExceptions.form(message, args);
        }

        @CheckReturnValue
        public static @NotNull IllegalArgumentException form(@NotNull String message, @Nullable Object @NotNull ... args) {
            return new IllegalArgumentException(message.formatted(args));
        }
    }

    public static class IllegalStateExceptions {
        public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
            if (!cond) {
                IllegalStateExceptions.fail(message, args);
            }
        }

        public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
            if (cond) {
                IllegalStateExceptions.fail(message, args);
            }
        }

        @CanIgnoreReturnValue
        public static <R> R fail(@NotNull String message, @Nullable Object @NotNull ... args) {
            throw IllegalStateExceptions.form(message, args);
        }

        @CheckReturnValue
        public static @NotNull IllegalStateException form(@NotNull String message, @Nullable Object @NotNull ... args) {
            return new IllegalStateException(message.formatted(args));
        }
    }
}
