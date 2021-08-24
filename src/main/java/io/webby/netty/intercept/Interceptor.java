package io.webby.netty.intercept;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.errors.ServeException;
import io.webby.netty.request.MutableHttpRequestEx;
import org.jetbrains.annotations.NotNull;

public interface Interceptor {
    default boolean isEnabled() {
        return true;
    }

    default void enter(@NotNull MutableHttpRequestEx request) throws ServeException {
        // Do nothing
    }

    default @NotNull HttpResponse exit(@NotNull MutableHttpRequestEx request, @NotNull HttpResponse response) {
        return response;
    }

    default void cleanup() {}
}
