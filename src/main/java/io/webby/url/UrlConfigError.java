package io.webby.url;

import io.webby.app.AppConfigException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UrlConfigError extends AppConfigException {
    public UrlConfigError(@NotNull String message) {
        super(message);
    }

    public UrlConfigError(@NotNull String message, @Nullable Object @NotNull ... args) {
        super(message.formatted(args));
    }

    public UrlConfigError(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public UrlConfigError(@Nullable Throwable cause) {
        super(cause);
    }

    public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (!cond) {
            throw new UrlConfigError(message, args);
        }
    }

    public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (cond) {
            throw new UrlConfigError(message, args);
        }
    }
}
