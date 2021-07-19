package io.webby.netty.exceptions;

public class RedirectException extends ServeException {
    private final String uri;
    private final boolean permanent;

    public RedirectException(String uri, boolean permanent) {
        this.uri = uri;
        this.permanent = permanent;
    }

    public RedirectException(String message, String uri, boolean permanent) {
        super(message);
        this.uri = uri;
        this.permanent = permanent;
    }

    public RedirectException(String message, Throwable cause, String uri, boolean permanent) {
        super(message, cause);
        this.uri = uri;
        this.permanent = permanent;
    }

    public RedirectException(Throwable cause, String uri, boolean permanent) {
        super(cause);
        this.uri = uri;
        this.permanent = permanent;
    }

    public RedirectException(String uri) {
        this(uri, true);
    }

    public RedirectException(String message, String uri) {
        this(message, uri, true);
    }

    public RedirectException(String message, Throwable cause, String uri) {
        this(message, cause, uri, true);
    }

    public RedirectException(Throwable cause, String uri) {
        this(cause, uri, true);
    }

    public String uri() {
        return uri;
    }

    public boolean isPermanent() {
        return permanent;
    }
}
