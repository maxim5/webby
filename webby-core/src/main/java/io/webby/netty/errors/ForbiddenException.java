package io.webby.netty.errors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForbiddenException extends ServeException {
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForbiddenException(Throwable cause) {
        super(cause);
    }

    public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (!cond) {
            throw new ForbiddenException(message.formatted(args));
        }
    }

    public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (cond) {
            throw new ForbiddenException(message.formatted(args));
        }
    }
}
