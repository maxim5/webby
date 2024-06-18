package io.spbx.webby.auth.session;

import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.spbx.webby.auth.user.DefaultUser;
import io.spbx.webby.auth.user.UserTable;
import io.spbx.webby.netty.request.HttpRequestEx;
import io.spbx.webby.testing.HttpRequestBuilder;
import io.spbx.webby.testing.UserBuilder;
import io.spbx.webby.testing.ext.SqlDbExtension;
import io.spbx.webby.auth.BaseCoreIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("slow") @Tag("sql")
public class SessionManagerIntegrationTest extends BaseCoreIntegrationTest {
    @RegisterExtension static final SqlDbExtension SQL = SqlDbExtension.fromProperties().withManualCleanup(UserTable.META, SessionTable.META);

    private static final DefaultUser DUMMY_USER = UserBuilder.ofId(123).build();
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
        assertThat(newSession.sessionId()).isEqualTo(session.sessionId());
        assertThat(newSession.createdAt()).isEqualTo(session.createdAt());
        assertThat(newSession.userAgent()).isEqualTo(session.userAgent());
        assertThat(newSession.ipAddress()).isEqualTo(session.ipAddress());
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
