package io.spbx.webby.netty.errors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class NotFoundException extends ServeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }

    public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (!cond) {
            throw new NotFoundException(message.formatted(args));
        }
    }

    public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (cond) {
            throw new NotFoundException(message.formatted(args));
        }
    }

    public static <T> @NotNull T getOrThrowNotFound(@NotNull Supplier<T> supplier,
                                                    @NotNull String message,
                                                    @Nullable Object @NotNull ... args) {
        T result = supplier.get();
        failIf(result == null, message, args);
        return result;
    }
}
