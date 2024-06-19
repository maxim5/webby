package io.spbx.webby.netty.intercept;

import io.spbx.webby.netty.errors.ServeException;
import io.spbx.webby.netty.request.MutableHttpRequestEx;
import io.spbx.webby.url.impl.Endpoint;
import org.jetbrains.annotations.NotNull;

public interface AdvancedInterceptor extends Interceptor {
    @Override
    default void enter(@NotNull MutableHttpRequestEx request) throws ServeException {
        throw new UnsupportedOperationException("Endpoint must be provided");
    }

    void enter(@NotNull MutableHttpRequestEx request, @NotNull Endpoint endpoint) throws ServeException;
}
