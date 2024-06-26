package io.spbx.webby.ws.context;

public interface BaseRequestContext {
    long requestId();

    boolean isTextRequest();

    default boolean isBinaryRequest() {
        return !isTextRequest();
    }
}
