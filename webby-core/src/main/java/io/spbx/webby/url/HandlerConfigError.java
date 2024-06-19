package io.spbx.webby.url;

import io.spbx.webby.app.AppConfigException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HandlerConfigError extends AppConfigException {
    public HandlerConfigError(@NotNull String message) {
        super(message);
    }

    public HandlerConfigError(@NotNull String message, @Nullable Object @NotNull ... args) {
        super(message.formatted(args));
    }

    public HandlerConfigError(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public HandlerConfigError(@Nullable Throwable cause) {
        super(cause);
    }

    public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (!cond) {
            throw new HandlerConfigError(message, args);
        }
    }

    public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (cond) {
            throw new HandlerConfigError(message, args);
        }
    }
}
