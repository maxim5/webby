package io.webby.ws;

public interface BaseRequestContext {
    long requestId();

    boolean isTextRequest();

    default boolean isBinaryRequest() {
        return !isTextRequest();
    }
}
