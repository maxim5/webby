package io.webby.auth.user;

import io.webby.auth.session.SessionStore;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.Testing;
import io.webby.testing.TestingModels;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CurrentUserManagerTest {
    private static final UserData USER_DATA = DefaultUser.newUserData(UserAccess.SuperAdmin);

    private final CurrentUserManager manager = Testing.testStartup().getInstance(CurrentUserManager.class);
    private final DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").ex();

    @Test
    public void without_session_fails() {
        assertThrows(AssertionError.class, () ->  manager.createAndBindCurrentUser(USER_DATA, request));
    }

    @Test
    public void invalid_user_data_fails() {
        request.setSession(TestingModels.newSession(123));

        assertThrows(AssertionError.class, () ->  manager.createAndBindCurrentUser(TestingModels.newUser(456), request));
        assertThat(getUserStore().size()).isEqualTo(0);
        assertThat(getSessionStore().size()).isEqualTo(0);
    }

    @Test
    public void with_authenticated_session_fails() {
        request.setSession(TestingModels.newSession(123));
        request.authenticate(TestingModels.newUser(456));

        assertThrows(AssertionError.class, () ->  manager.createAndBindCurrentUser(USER_DATA, request));
        assertThat(getUserStore().size()).isEqualTo(0);
        assertThat(getSessionStore().size()).isEqualTo(0);
    }

    @Test
    public void with_unauthenticated_session_success() {
        request.setSession(TestingModels.newSession(123));

        UserModel user = manager.createAndBindCurrentUser(USER_DATA, request);
        int userId = user.userId();

        assertThat(userId).isGreaterThan(0);
        assertThat(user).isEqualTo(USER_DATA.toUserModel(userId));
        assertThat(request.isAuthenticated());
        assertThat(request.<UserModel>user()).isEqualTo(user);

        assertThat(getUserStore().size()).isEqualTo(1);
        assertThat(getUserStore().getUserByIdOrNull(userId)).isEqualTo(user);
        assertThat(getSessionStore().size()).isEqualTo(1);
        assertThat(getSessionStore().getSessionByIdOrNull(request.session().sessionId())).isEqualTo(request.session());
    }

    private static @NotNull UserStore getUserStore() {
        return Testing.Internals.getInstance(UserStore.class);
    }

    private static @NotNull SessionStore getSessionStore() {
        return Testing.Internals.getInstance(SessionStore.class);
    }
}
