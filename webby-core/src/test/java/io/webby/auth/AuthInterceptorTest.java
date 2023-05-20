package io.webby.auth;

import com.google.inject.Injector;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserAccess;
import io.webby.auth.user.UserModel;
import io.webby.auth.user.UserStore;
import io.webby.netty.errors.NotFoundException;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.testing.FakeEndpoints;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.SessionBuilder;
import io.webby.testing.Testing;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuthInterceptorTest {
    private final Injector injector = Testing.testStartup();
    private final AuthInterceptor interceptor = injector.getInstance(AuthInterceptor.class);
    private final UserStore userStore = injector.getInstance(UserStore.class);

    @Test
    public void lifecycle_enter_exit_session_unauthenticated() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").ex();
        request.setSession(SessionBuilder.ofId(555).build());

        interceptor.enter(request, FakeEndpoints.fakeEndpoint());
        assertThat(request.isAuthenticated()).isFalse();
        assertThat(request.<UserModel>user()).isNull();
        assertThat(userStore.isEmpty()).isTrue();
    }

    @Test
    public void lifecycle_enter_exit_session_authenticated() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").ex();
        UserModel user = userStore.createUserAutoId(DefaultUser.newUserData(UserAccess.Simple));
        request.setSession(SessionBuilder.ofId(555).withUser(user).build());

        interceptor.enter(request, FakeEndpoints.fakeEndpoint());
        assertThat(request.isAuthenticated()).isTrue();
        assertThat(request.<UserModel>user()).isSameInstanceAs(user);
        assertThat(userStore.size()).isEqualTo(1);
        assertThat(userStore.getUserByIdOrNull(user.userId())).isEqualTo(user);
    }

    @Test
    public void lifecycle_enter_exit_session_with_non_existing_user() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").ex();
        request.setSession(SessionBuilder.ofId(555).withUserId(999).build());

        assertThrows(NotFoundException.class, () -> interceptor.enter(request, FakeEndpoints.fakeEndpoint()));
        assertThat(userStore.isEmpty()).isTrue();
    }
}
