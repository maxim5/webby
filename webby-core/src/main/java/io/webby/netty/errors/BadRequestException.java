package io.webby.netty.errors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BadRequestException extends ServeException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }

    public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (!cond) {
            throw new BadRequestException(message.formatted(args));
        }
    }

    public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (cond) {
            throw new BadRequestException(message.formatted(args));
        }
    }
}
