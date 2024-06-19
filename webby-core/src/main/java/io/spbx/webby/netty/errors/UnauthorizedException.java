package io.spbx.webby.netty.errors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnauthorizedException extends ServeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(Throwable cause) {
        super(cause);
    }

    public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (!cond) {
            throw new UnauthorizedException(message.formatted(args));
        }
    }

    public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (cond) {
            throw new UnauthorizedException(message.formatted(args));
        }
    }
}
