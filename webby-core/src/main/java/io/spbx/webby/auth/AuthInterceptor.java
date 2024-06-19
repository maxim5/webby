package io.spbx.webby.auth;

import com.google.inject.Inject;
import io.spbx.webby.auth.session.SessionModel;
import io.spbx.webby.auth.user.UserModel;
import io.spbx.webby.auth.user.UserStore;
import io.spbx.webby.netty.errors.ServeException;
import io.spbx.webby.netty.errors.UnauthorizedException;
import io.spbx.webby.netty.intercept.AdvancedInterceptor;
import io.spbx.webby.netty.intercept.attr.AttributeOwner;
import io.spbx.webby.netty.intercept.attr.Attributes;
import io.spbx.webby.netty.request.MutableHttpRequestEx;
import io.spbx.webby.url.impl.Endpoint;
import io.spbx.webby.url.impl.EndpointOptions;
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
