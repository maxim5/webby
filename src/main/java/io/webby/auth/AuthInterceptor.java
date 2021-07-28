package io.webby.auth;

import io.webby.netty.exceptions.ServeException;
import io.webby.netty.intercept.Interceptor;
import io.webby.netty.intercept.attr.AttributeOwner;
import io.webby.netty.intercept.attr.Attributes;
import io.webby.netty.request.MutableHttpRequestEx;
import org.jetbrains.annotations.NotNull;

@AttributeOwner(position = Attributes.Auth)
public class AuthInterceptor implements Interceptor {
    @Override
    public void enter(@NotNull MutableHttpRequestEx request) throws ServeException {
        // empty so far
    }
}
