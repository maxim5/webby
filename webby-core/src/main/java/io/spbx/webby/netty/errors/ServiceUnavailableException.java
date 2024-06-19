package io.spbx.webby.netty.errors;

public class ServiceUnavailableException extends ServeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
