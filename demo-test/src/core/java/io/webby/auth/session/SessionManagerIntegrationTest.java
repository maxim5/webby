package io.webby.auth.session;

import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.webby.auth.BaseCoreIntegrationTest;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserTable;
import io.webby.netty.request.HttpRequestEx;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.TestingModels;
import io.webby.testing.ext.SqlDbExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("sql")
public class SessionManagerIntegrationTest extends BaseCoreIntegrationTest {
    @RegisterExtension static final SqlDbExtension SQL = SqlDbExtension.fromProperties().withManualCleanup(UserTable.META, SessionTable.META);

    private static final DefaultUser DUMMY_USER = TestingModels.newUser(123);
    private static final HttpRequestEx GET = HttpRequestBuilder.get("/").ex();

    @BeforeEach
    void setUp() {
        new UserTable(SQL).insert(DUMMY_USER);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void getOrCreateSession_null(Scenario scenario) {
        SessionManager manager = startup(scenario, SQL.settings()).getInstance(SessionManager.class);
        SessionModel session = manager.getOrCreateSession(GET, null);
        assertThat(session.hasUserId()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void getOrCreateSession_invalid_cookie(Scenario scenario) {
        SessionManager manager = startup(scenario, SQL.settings()).getInstance(SessionManager.class);
        SessionModel session = manager.getOrCreateSession(GET, new DefaultCookie("name", "foo"));
        assertThat(session.hasUserId()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void getOrCreateSession_valid_cookie(Scenario scenario) {
        SessionManager manager = startup(scenario, SQL.settings()).getInstance(SessionManager.class);
        SessionModel session = manager.createNewSession(GET);
        String encoded = manager.encodeSessionForCookie(session);
        SessionModel returned = manager.getOrCreateSession(GET, new DefaultCookie("name", encoded));
        assertThat(session).isEqualTo(returned);
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    public void addUserOrDie_no_user(Scenario scenario) {
        SessionManager manager = startup(scenario, SQL.settings()).getInstance(SessionManager.class);
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
        SessionManager manager = startup(scenario, SQL.settings()).getInstance(SessionManager.class);
        SessionModel session = manager.createNewSession(GET);
        SessionModel newSession = manager.addUserOrDie(session, DUMMY_USER);
        assertThat(newSession.hasUserId()).isTrue();

        assertThrows(AssertionError.class, () -> manager.addUserOrDie(newSession, DUMMY_USER));
    }
}
