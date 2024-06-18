package io.spbx.webby.netty.errors;

public abstract class ServeException extends RuntimeException {
    public ServeException() {
        super();
    }

    public ServeException(String message) {
        super(message);
    }

    public ServeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServeException(Throwable cause) {
        super(cause);
    }
}
