package io.webby.auth.user;

import com.google.inject.Inject;
import io.webby.auth.session.SessionManager;
import io.webby.auth.session.SessionModel;
import io.webby.netty.request.HttpRequestEx;
import io.webby.netty.request.MutableHttpRequestEx;
import org.jetbrains.annotations.NotNull;

public class CurrentUserManager {
    private final UserStore users;
    private final SessionManager sessions;

    @Inject
    public CurrentUserManager(@NotNull UserStore users, @NotNull SessionManager sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    public @NotNull UserModel createAndBindCurrentUser(@NotNull UserData data, @NotNull HttpRequestEx request) {
        assert !(data instanceof UserModel userModel) || userModel.isAutoId() : "User is not auto-id: %s".formatted(data);
        assert request.user() == null : "Request already has a current user: " + request.user();
        assert !request.isAuthenticated() : "Request is already authenticated: " + request;
        SessionModel session = request.session();
        UserModel currentUser = users.createUserAutoId(data);
        sessions.addUserOrDie(session, currentUser);
        if (request instanceof MutableHttpRequestEx mutable) {
            mutable.authenticate(currentUser);
        }
        return currentUser;
    }
}
