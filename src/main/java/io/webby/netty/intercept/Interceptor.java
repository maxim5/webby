package io.webby.netty.intercept;

import io.netty.handler.codec.http.FullHttpResponse;
import io.webby.netty.exceptions.ServeException;
import io.webby.netty.request.HttpRequestEx;
import org.jetbrains.annotations.NotNull;

public interface Interceptor {
    default void enter(@NotNull HttpRequestEx request) throws ServeException {
        // Do nothing
    }

    @NotNull
    default FullHttpResponse exit(@NotNull HttpRequestEx request, @NotNull FullHttpResponse response) {
        return response;
    }
}
