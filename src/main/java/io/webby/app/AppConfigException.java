package io.webby.app;

public class AppConfigException extends RuntimeException {
    public AppConfigException() {
    }

    public AppConfigException(String message) {
        super(message);
    }

    public AppConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppConfigException(Throwable cause) {
        super(cause);
    }

    public static void failIf(boolean cond, String message) {
        if (cond) {
            throw new AppConfigException(message);
        }
    }
}
