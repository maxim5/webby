package io.webby.auth;

import com.google.inject.Inject;
import io.webby.auth.session.SessionModel;
import io.webby.auth.user.UserModel;
import io.webby.auth.user.UserStore;
import io.webby.netty.errors.ServeException;
import io.webby.netty.errors.UnauthorizedException;
import io.webby.netty.intercept.AdvancedInterceptor;
import io.webby.netty.intercept.attr.AttributeOwner;
import io.webby.netty.intercept.attr.Attributes;
import io.webby.netty.request.MutableHttpRequestEx;
import io.webby.url.impl.Endpoint;
import io.webby.url.impl.EndpointOptions;
import org.jetbrains.annotations.NotNull;

@AttributeOwner(position = Attributes.User)
public class AuthInterceptor implements AdvancedInterceptor {
    @Inject private UserStore users;

    @Override
    public void enter(@NotNull MutableHttpRequestEx request, @NotNull Endpoint endpoint) throws ServeException {
        UserModel user = null;
        SessionModel session = request.session();
        if (session.isAuthenticated()) {
            user = users.getUserByIdOr404(session.user());  // TODO: invalidate the session instead of throwing 404
            request.setUser(user);
        }

        EndpointOptions options = endpoint.options();
        UnauthorizedException.failIf(user == null && options.requiresAuth(),
                                     "Authorization required for url=`%s`", request.uri());
        UnauthorizedException.failIf(user != null && options.access() > user.access().level(),
                                     "Insufficient access level for url=`%s`, user=`%s`", request.uri(), user);
    }
}
