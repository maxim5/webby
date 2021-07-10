package io.webby.url.caller;

public class CallException extends RuntimeException {
    public CallException(String message) {
        super(message);
    }

    public CallException(String message, Throwable cause) {
        super(message, cause);
    }

    public CallException(Throwable cause) {
        super(cause);
    }
}
