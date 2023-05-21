package io.webby.auth.session;

import io.webby.auth.user.DefaultUser;
import io.webby.netty.request.HttpRequestEx;
import io.webby.orm.api.ForeignInt;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.Mocking;
import io.webby.testing.SessionBuilder;
import io.webby.testing.UserBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.ScopedMock;

import java.time.Instant;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.db.model.LongAutoIdModel.AUTO_ID;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultSessionTest {
    private static final String USER_AGENT = "foobar";
    private static final String IP = null;
    private static final HttpRequestEx REQUEST = HttpRequestBuilder.get("/foo").withUserAgent(USER_AGENT).ex();
    private static final Instant INSTANT = Mocking.nowTruncatedToMillis();
    private static final DefaultUser USER = UserBuilder.ofId(777).build();

    @Test
    public void newSessionData_simple() {
        try (ScopedMock ignore = Mocking.withMockedInstantNow(INSTANT)) {
            SessionData data = DefaultSession.newSessionData(REQUEST);
            DefaultSession expected = new DefaultSession(AUTO_ID, ForeignInt.empty(), INSTANT, USER_AGENT, IP);
            assertThat(data).isEqualTo(expected);
        }
    }

    @Test
    public void toSessionModel_success() {
        try (ScopedMock ignore = Mocking.withMockedInstantNow(INSTANT)) {
            SessionData data = DefaultSession.newSessionData(REQUEST);
            SessionModel model = data.toSessionModel(111);
            DefaultSession expected = new DefaultSession(111, ForeignInt.empty(), INSTANT, USER_AGENT, IP);
            assertThat(model).isEqualTo(expected);
        }
    }

    @Test
    public void toSessionModel_fails() {
        SessionData data = SessionBuilder.ofId(222).build();
        assertThrows(AssertionError.class, () -> data.toSessionModel(333));
    }

    @Test
    public void withUser_empty() {
        DefaultSession session = new DefaultSession(AUTO_ID, ForeignInt.empty(), INSTANT, USER_AGENT, IP);
        DefaultSession expected = new DefaultSession(AUTO_ID, USER.toForeignInt(), INSTANT, USER_AGENT, IP);
        assertThat(session.withUser(USER)).isEqualTo(expected);
        assertThat(session.user().getEntity()).isNull();
    }

    @Test
    public void withUser_has_matching_id() {
        DefaultSession session = new DefaultSession(AUTO_ID, ForeignInt.ofId(USER.userId()), INSTANT, USER_AGENT, IP);
        DefaultSession expected = new DefaultSession(AUTO_ID, USER.toForeignInt(), INSTANT, USER_AGENT, IP);
        assertThat(session.withUser(USER)).isEqualTo(expected);
        assertThat(session.user().getEntity()).isEqualTo(USER);  // side-effect
    }

    @Test
    public void withUser_has_matching_entity() {
        DefaultSession session = new DefaultSession(AUTO_ID, USER.toForeignInt(), INSTANT, USER_AGENT, IP);
        DefaultSession expected = new DefaultSession(AUTO_ID, USER.toForeignInt(), INSTANT, USER_AGENT, IP);
        assertThat(session.withUser(USER)).isEqualTo(expected);
        assertThat(session.user().getEntity()).isEqualTo(USER);  // side-effect
    }

    @Test
    public void withUser_has_different_id() {
        DefaultSession session = new DefaultSession(AUTO_ID, ForeignInt.ofId(999), INSTANT, USER_AGENT, IP);
        DefaultSession expected = new DefaultSession(AUTO_ID, USER.toForeignInt(), INSTANT, USER_AGENT, IP);
        assertThat(session.withUser(USER)).isEqualTo(expected);
        assertThat(session.user().getEntity()).isNull();
    }
}
