package io.spbx.webby.auth.user;

import com.google.inject.Injector;
import io.spbx.webby.auth.session.SessionTable;
import io.spbx.webby.netty.request.DefaultHttpRequestEx;
import io.spbx.webby.testing.HttpRequestBuilder;
import io.spbx.webby.testing.SessionBuilder;
import io.spbx.webby.testing.UserBuilder;
import io.spbx.webby.testing.ext.SqlDbExtension;
import io.webby.auth.BaseCoreIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("slow") @Tag("sql")
public class CurrentUserManagerIntegrationTest extends BaseCoreIntegrationTest {
    @RegisterExtension static final SqlDbExtension SQL = SqlDbExtension.fromProperties().withManualCleanup(UserTable.META, SessionTable.META);
    private static final UserData USER_DATA = DefaultUser.newUserData(UserAccess.SuperAdmin);

    private final DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").ex();

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void without_session_fails(Scenario scenario) {
        CurrentUserManager manager = startup(scenario, SQL.settings()).getInstance(CurrentUserManager.class);
        assertThrows(AssertionError.class, () ->  manager.createAndBindCurrentUser(USER_DATA, request));
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void invalid_user_data_fails(Scenario scenario) {
        Injector injector = startup(scenario, SQL.settings());
        CurrentUserManager manager = injector.getInstance(CurrentUserManager.class);
        request.setSession(SessionBuilder.simple(123));

        assertThrows(AssertionError.class, () -> manager.createAndBindCurrentUser(UserBuilder.simple(456), request));
        assertThat(getUserStore().size()).isEqualTo(0);
        assertThat(getSessionStore().size()).isEqualTo(0);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void with_authenticated_session_fails(Scenario scenario) {
        CurrentUserManager manager = startup(scenario, SQL.settings()).getInstance(CurrentUserManager.class);
        request.setSession(SessionBuilder.simple(123));
        request.authenticate(UserBuilder.simple(456));

        assertThrows(AssertionError.class, () ->  manager.createAndBindCurrentUser(USER_DATA, request));
        assertThat(getUserStore().size()).isEqualTo(0);
        assertThat(getSessionStore().size()).isEqualTo(0);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void with_unauthenticated_session_success(Scenario scenario) {
        CurrentUserManager manager = startup(scenario, SQL.settings()).getInstance(CurrentUserManager.class);
        request.setSession(SessionBuilder.simple(123));

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
}
