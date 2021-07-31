package io.webby.auth;

import io.webby.netty.exceptions.ServeException;
import io.webby.netty.exceptions.UnauthorizedException;
import io.webby.netty.intercept.AdvancedInterceptor;
import io.webby.netty.intercept.attr.AttributeOwner;
import io.webby.netty.intercept.attr.Attributes;
import io.webby.netty.request.MutableHttpRequestEx;
import io.webby.url.impl.EndpointCaller;
import org.jetbrains.annotations.NotNull;

@AttributeOwner(position = Attributes.User)
public class AuthInterceptor implements AdvancedInterceptor {
    @Override
    public void enter(@NotNull MutableHttpRequestEx request, @NotNull EndpointCaller endpoint) throws ServeException {
        if (endpoint.options().requiresAuth()) {
            throw new UnauthorizedException("Endpoint requires authorization: %s".formatted(request.uri()));
        }

        // request.setAttr(Attributes.User, user);
    }
}
