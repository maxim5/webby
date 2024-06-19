package io.spbx.webby.netty.errors;

import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpCodeException extends ServeException {
    private final HttpResponseStatus status;

    public HttpCodeException(HttpResponseStatus status) {
        this.status = status;
    }

    public HttpCodeException(String message, HttpResponseStatus status) {
        super(message);
        this.status = status;
    }

    public HttpCodeException(String message, Throwable cause, HttpResponseStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpCodeException(Throwable cause, HttpResponseStatus status) {
        super(cause);
        this.status = status;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }
}
