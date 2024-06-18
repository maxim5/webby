package io.spbx.webby.url.view;

public class RenderException extends RuntimeException {
    public RenderException(String message) {
        super(message);
    }

    public RenderException(String message, Throwable cause) {
        super(message, cause);
    }

    public RenderException(Throwable cause) {
        super(cause);
    }

    public static void failIf(boolean condition, String message) {
        if (condition) {
            throw new RenderException(message);
        }
    }
}
