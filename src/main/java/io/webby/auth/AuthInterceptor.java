package io.webby.auth;

import com.google.inject.Inject;
import io.webby.auth.session.Session;
import io.webby.auth.user.UserModel;
import io.webby.auth.user.UserManager;
import io.webby.netty.errors.ServeException;
import io.webby.netty.errors.UnauthorizedException;
import io.webby.netty.intercept.AdvancedInterceptor;
import io.webby.netty.intercept.attr.AttributeOwner;
import io.webby.netty.intercept.attr.Attributes;
import io.webby.netty.request.MutableHttpRequestEx;
import io.webby.url.impl.Endpoint;
import org.jetbrains.annotations.NotNull;

@AttributeOwner(position = Attributes.User)
public class AuthInterceptor implements AdvancedInterceptor {
    @Inject private UserManager userManager;

    @Override
    public void enter(@NotNull MutableHttpRequestEx request, @NotNull Endpoint endpoint) throws ServeException {
        if (endpoint.options().requiresAuth()) {
            throw new UnauthorizedException("Endpoint requires authorization: %s".formatted(request.uri()));
        }

        Session session = request.session();
        if (session.hasUser()) {
            UserModel user = userManager.findByUserId(session.user());
            request.setNullableAttr(Attributes.User, user);
        }
    }
}
