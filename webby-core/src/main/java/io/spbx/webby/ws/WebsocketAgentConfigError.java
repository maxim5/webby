package io.spbx.webby.ws;

import io.spbx.webby.app.AppConfigException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WebsocketAgentConfigError extends AppConfigException {
    public WebsocketAgentConfigError(@NotNull String message) {
        super(message);
    }

    public WebsocketAgentConfigError(@NotNull String message, @Nullable Object @NotNull ... args) {
        super(message.formatted(args));
    }

    public WebsocketAgentConfigError(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public WebsocketAgentConfigError(@Nullable Throwable cause) {
        super(cause);
    }

    public static void assure(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (!cond) {
            throw new WebsocketAgentConfigError(message, args);
        }
    }

    public static void failIf(boolean cond, @NotNull String message, @Nullable Object @NotNull ... args) {
        if (cond) {
            throw new WebsocketAgentConfigError(message, args);
        }
    }
}
