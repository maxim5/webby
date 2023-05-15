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

import static io.webby.testing.TestingModels.assertSameSession;
import static org.junit.jupiter.api.Assertions.*;

public class SessionManagerIntegrationTest {
    @RegisterExtension static final SqlDbSetupExtension SQL = SqlDbSetupExtension.fromProperties();

    private static final UserModel DUMMY_USER = TestingModels.newUserNow(123);
    private static final HttpRequestEx GET = HttpRequestBuilder.get("/").ex();

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void getOrCreateSession_null(Scenario scenario) {
        SessionManager manager = startup(scenario);
        Session session = manager.getOrCreateSession(GET, null);
        assertFalse(session.hasUser());
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void getOrCreateSession_invalid_cookie(Scenario scenario) {
        SessionManager manager = startup(scenario);
        Session session = manager.getOrCreateSession(GET, new DefaultCookie("name", "foo"));
        assertFalse(session.hasUser());
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void getOrCreateSession_valid_cookie(Scenario scenario) {
        SessionManager manager = startup(scenario);
        Session session = manager.createNewSession(GET);
        String encoded = manager.encodeSessionForCookie(session);
        Session returned = manager.getOrCreateSession(GET, new DefaultCookie("name", encoded));
        assertSameSession(session, returned);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void addUserOrDie_no_user(Scenario scenario) {
        SessionManager manager = startup(scenario);
        Session session = manager.createNewSession(GET);
        assertFalse(session.hasUser());

        Session newSession = manager.addUserOrDie(session, DUMMY_USER);
        assertEquals(newSession.sessionId(), session.sessionId());
        assertEquals(newSession.createdAt(), session.createdAt());
        assertEquals(newSession.userAgent(), session.userAgent());
        assertEquals(newSession.ipAddress(), session.ipAddress());
        assertTrue(newSession.hasUser());

        String encoded = manager.encodeSessionForCookie(newSession);
        Session returned = manager.getOrCreateSession(GET, new DefaultCookie("name", encoded));
        assertSameSession(newSession, returned);
        assertTrue(returned.hasUser());
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void addUserOrDie_with_user_throws(Scenario scenario) {
        SessionManager manager = startup(scenario);
        Session session = manager.createNewSession(GET);
        Session newSession = manager.addUserOrDie(session, DUMMY_USER);
        assertTrue(newSession.hasUser());

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
