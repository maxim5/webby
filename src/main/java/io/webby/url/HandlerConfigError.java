package io.webby.url;

import io.webby.app.AppConfigException;

public class HandlerConfigError extends AppConfigException {
    public HandlerConfigError(String message) {
        super(message);
    }

    public HandlerConfigError(String message, Throwable cause) {
        super(message, cause);
    }

    public HandlerConfigError(Throwable cause) {
        super(cause);
    }

    public static void failIf(boolean cond, String message) {
        if (cond) {
            throw new HandlerConfigError(message);
        }
    }
}
