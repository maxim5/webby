package io.webby.auth.session;

import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.webby.app.AppSettings;
import io.webby.auth.user.UserModel;
import io.webby.netty.request.HttpRequestEx;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.Testing;
import io.webby.testing.TestingModels;
import io.webby.testing.TestingStorage;
import io.webby.testing.ext.SqlDbSetupExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SessionManagerIntegrationTest {
    @RegisterExtension static final SqlDbSetupExtension SQL = SqlDbSetupExtension.fromProperties();

    private static final UserModel DUMMY_USER = TestingModels.newUser(123);
    private static final HttpRequestEx GET = HttpRequestBuilder.get("/").ex();

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void getOrCreateSession_null(Scenario scenario) {
        SessionManager manager = startup(scenario);
        SessionModel session = manager.getOrCreateSession(GET, null);
        assertThat(session.hasUserId()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void getOrCreateSession_invalid_cookie(Scenario scenario) {
        SessionManager manager = startup(scenario);
        SessionModel session = manager.getOrCreateSession(GET, new DefaultCookie("name", "foo"));
        assertThat(session.hasUserId()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void getOrCreateSession_valid_cookie(Scenario scenario) {
        SessionManager manager = startup(scenario);
        SessionModel session = manager.createNewSession(GET);
        String encoded = manager.encodeSessionForCookie(session);
        SessionModel returned = manager.getOrCreateSession(GET, new DefaultCookie("name", encoded));
        assertThat(session).isEqualTo(returned);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void addUserOrDie_no_user(Scenario scenario) {
        SessionManager manager = startup(scenario);
        SessionModel session = manager.createNewSession(GET);
        assertThat(session.hasUserId()).isFalse();

        SessionModel newSession = manager.addUserOrDie(session, DUMMY_USER);
        assertEquals(newSession.sessionId(), session.sessionId());
        assertEquals(newSession.createdAt(), session.createdAt());
        assertEquals(newSession.userAgent(), session.userAgent());
        assertEquals(newSession.ipAddress(), session.ipAddress());
        assertThat(newSession.hasUserId()).isTrue();

        String encoded = manager.encodeSessionForCookie(newSession);
        SessionModel returned = manager.getOrCreateSession(GET, new DefaultCookie("name", encoded));
        assertThat(newSession).isEqualTo(returned);
        assertThat(returned.hasUserId()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void addUserOrDie_with_user_throws(Scenario scenario) {
        SessionManager manager = startup(scenario);
        SessionModel session = manager.createNewSession(GET);
        SessionModel newSession = manager.addUserOrDie(session, DUMMY_USER);
        assertThat(newSession.hasUserId()).isTrue();

        assertThrows(AssertionError.class, () -> manager.addUserOrDie(newSession, DUMMY_USER));
    }

    private static @NotNull SessionManager startup(@NotNull Scenario scenario) {
        AppSettings settings = Testing.defaultAppSettings();
        settings.modelFilter().setCommonPackageOf(Testing.CORE_MODELS);
        switch (scenario) {
            case SQL -> settings.storageSettings().enableSql(SQL.settings()).enableKeyValue(TestingStorage.KEY_VALUE_DEFAULT);
            case KEY_VALUE -> settings.storageSettings().enableKeyValue(TestingStorage.KEY_VALUE_DEFAULT);
        }
        return Testing.testStartup(settings, SQL::savepoint, SQL.combinedTestingModule()).getInstance(SessionManager.class);
    }

    private enum Scenario {
        SQL,
        KEY_VALUE,
    }
}
