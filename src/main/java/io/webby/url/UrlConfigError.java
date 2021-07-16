package io.webby.url;

import io.webby.app.AppConfigException;

public class UrlConfigError extends AppConfigException {
    public UrlConfigError(String message) {
        super(message);
    }

    public UrlConfigError(String message, Throwable cause) {
        super(message, cause);
    }

    public UrlConfigError(Throwable cause) {
        super(cause);
    }

    public static void failIf(boolean cond, String message) {
        if (cond) {
            throw new UrlConfigError(message);
        }
    }
}
