package io.spbx.util.base;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EasyExceptions {
    public static class AssertionErrors {
        @CanIgnoreReturnValue
        public static <R> R fail(@NotNull String message, @Nullable Object @NotNull ... args) {
            throw AssertionErrors.format(message, args);
        }

        @CheckReturnValue
        public static @NotNull AssertionError format(@NotNull String message, @Nullable Object @NotNull ... args) {
            return new AssertionError(message.formatted(args));
        }
    }

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
            throw IllegalArgumentExceptions.format(message, args);
        }

        @CheckReturnValue
        public static @NotNull IllegalArgumentException format(@NotNull String message, @Nullable Object @NotNull ... args) {
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
            throw IllegalStateExceptions.format(message, args);
        }

        @CheckReturnValue
        public static @NotNull IllegalStateException format(@NotNull String message, @Nullable Object @NotNull ... args) {
            return new IllegalStateException(message.formatted(args));
        }
    }
}
