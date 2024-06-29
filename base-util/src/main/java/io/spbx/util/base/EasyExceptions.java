package io.spbx.util.base;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EasyExceptions {
    @CheckReturnValue
    public static @NotNull AssertionError newAssertionError(@NotNull String message, @Nullable Object @NotNull ... args) {
        return new AssertionError(message.formatted(args));
    }

    @CheckReturnValue
    public static @NotNull InternalError newInternalError(@NotNull String message, @Nullable Object @NotNull ... args) {
        return new InternalError(message.formatted(args));
    }

    @CheckReturnValue
    public static @NotNull IllegalArgumentException newIllegalArgumentException(@NotNull String message,
                                                                                @Nullable Object @NotNull ... args) {
        return new IllegalArgumentException(message.formatted(args));
    }

    @CheckReturnValue
    public static @NotNull IllegalStateException newIllegalStateException(@NotNull String message,
                                                                          @Nullable Object @NotNull ... args) {
        return new IllegalStateException(message.formatted(args));
    }

    public static class AssertionErrors {
        @CanIgnoreReturnValue
        public static <R> R fail(@NotNull String message, @Nullable Object @NotNull ... args) {
            throw newAssertionError(message, args);
        }
    }

    public static class InternalErrors {
        public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
            if (!cond) {
                InternalErrors.fail(message, args);
            }
        }

        public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
            if (cond) {
                InternalErrors.fail(message, args);
            }
        }

        @CanIgnoreReturnValue
        public static <R> R fail(@NotNull String message, @Nullable Object @NotNull ... args) {
            throw newInternalError(message, args);
        }
    }

    public static class IllegalArgumentExceptions {
        public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
            if (!cond) {
                throw newIllegalArgumentException(message, args);
            }
        }

        public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
            if (cond) {
                throw newIllegalArgumentException(message, args);
            }
        }

        @CanIgnoreReturnValue
        public static <R> R fail(@NotNull String message, @Nullable Object @NotNull ... args) {
            throw newIllegalArgumentException(message.formatted(args));
        }
    }

    public static class IllegalStateExceptions {
        public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
            if (!cond) {
                throw newIllegalStateException(message, args);
            }
        }

        public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
            if (cond) {
                throw newIllegalStateException(message, args);
            }
        }

        @CanIgnoreReturnValue
        public static <R> R fail(@NotNull String message, @Nullable Object @NotNull ... args) {
            throw newIllegalStateException(message, args);
        }
    }
}
