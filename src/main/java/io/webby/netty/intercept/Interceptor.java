package io.webby.netty.intercept;

import io.netty.handler.codec.http.FullHttpResponse;
import io.webby.netty.exceptions.ServeException;
import io.webby.netty.request.MutableHttpRequestEx;
import org.jetbrains.annotations.NotNull;

public interface Interceptor {
    default boolean isEnabled() {
        return true;
    }

    default void enter(@NotNull MutableHttpRequestEx request) throws ServeException {
        // Do nothing
    }

    @NotNull
    default FullHttpResponse exit(@NotNull MutableHttpRequestEx request, @NotNull FullHttpResponse response) {
        return response;
    }
}
