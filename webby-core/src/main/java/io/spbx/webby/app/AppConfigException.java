package io.spbx.webby.app;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AppConfigException extends RuntimeException {
    protected AppConfigException() {
    }

    public AppConfigException(@NotNull String message) {
        super(message);
    }

    public AppConfigException(@NotNull String message, @Nullable Object @NotNull ... args) {
        super(message.formatted(args));
    }

    public AppConfigException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public AppConfigException(@Nullable Throwable cause) {
        super(cause);
    }

    public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (!cond) {
            throw new AppConfigException(message, args);
        }
    }

    public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (cond) {
            throw new AppConfigException(message, args);
        }
    }
}
