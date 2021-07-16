package io.webby.url.validate;

public class ValidationError extends RuntimeException {
    public ValidationError(String message) {
        super(message);
    }

    public ValidationError(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationError(Throwable cause) {
        super(cause);
    }

    public static void failIf(boolean cond, String message) {
        if (cond) {
            throw new ValidationError(message);
        }
    }
}
