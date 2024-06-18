package io.spbx.webby.auth.user;

import com.google.inject.Inject;
import io.spbx.webby.auth.session.SessionManager;
import io.spbx.webby.auth.session.SessionModel;
import io.spbx.webby.db.sql.tx.TxRunner;
import io.spbx.webby.netty.request.HttpRequestEx;
import io.spbx.webby.netty.request.MutableHttpRequestEx;
import org.jetbrains.annotations.NotNull;

public class CurrentUserManager {
    private final UserStore users;
    private final SessionManager sessions;
    private final TxRunner txRunner;

    @Inject
    public CurrentUserManager(@NotNull UserStore users, @NotNull SessionManager sessions, @NotNull TxRunner txRunner) {
        this.users = users;
        this.sessions = sessions;
        this.txRunner = txRunner;
    }

    public @NotNull UserModel createAndBindCurrentUser(@NotNull UserData data, @NotNull HttpRequestEx request) {
        assert !(data instanceof UserModel userModel) || userModel.isAutoId() : "User is not auto-id: %s".formatted(data);
        assert request.user() == null : "Request already has a current user: " + request.user();
        assert !request.isAuthenticated() : "Request is already authenticated: " + request;
        SessionModel session = request.session();

        UserModel currentUser = txRunner.run(() -> {
            UserModel user = users.createUserAutoId(data);
            sessions.addUserOrDie(session, user);
            return user;
        });

        if (request instanceof MutableHttpRequestEx mutable) {
            mutable.authenticate(currentUser);
        }
        return currentUser;
    }
}
