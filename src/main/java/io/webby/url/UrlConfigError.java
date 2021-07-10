package io.webby.url;

public class UrlConfigError extends RuntimeException {
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
