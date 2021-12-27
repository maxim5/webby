package io.webby.auth.session;

import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.webby.auth.user.User;
import io.webby.netty.request.HttpRequestEx;
import io.webby.testing.FakeRequests;
import io.webby.testing.Testing;
import io.webby.testing.TestingModels;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SessionManagerTest {
    private static final User DUMMY_USER = TestingModels.newUserNow(123);

    private final SessionManager manager = Testing.testStartup().getInstance(SessionManager.class);
    private final HttpRequestEx GET = FakeRequests.getEx("/");

    @Test
    public void encode_decode_session_id() {
        int expectedLength = 22;

        assertEncodeDecode(0, expectedLength);
        assertEncodeDecode(42, expectedLength);
        assertEncodeDecode(-42, expectedLength);

        assertEncodeDecode(Long.MAX_VALUE, expectedLength);
        assertEncodeDecode(Long.MIN_VALUE, expectedLength);
    }

    @Test
    public void decode_invalid_session_value() {
        assertThrows(RuntimeException.class, () -> manager.decodeSessionId(""));
        assertThrows(RuntimeException.class, () -> manager.decodeSessionId("foo"));
        assertThrows(RuntimeException.class, () -> manager.decodeSessionId("12345"));
    }

    @Test
    public void getOrCreateSession_null() {
        Session session = manager.getOrCreateSession(GET, null);
        assertFalse(session.hasUser());
    }

    @Test
    public void getOrCreateSession_invalid_cookie() {
        Session session = manager.getOrCreateSession(GET, new DefaultCookie("name", "foo"));
        assertFalse(session.hasUser());
    }

    @Test
    public void getOrCreateSession_valid_cookie() {
        Session session = manager.createNewSession(GET);
        String encoded = manager.encodeSessionForCookie(session);
        Session returned = manager.getOrCreateSession(GET, new DefaultCookie("name", encoded));
        assertEquals(session, returned);
    }

    @Test
    public void addUserOrDie_no_user() {
        Session session = manager.createNewSession(GET);
        assertFalse(session.hasUser());

        Session newSession = manager.addUserOrDie(session, DUMMY_USER);
        assertEquals(newSession.sessionId(), session.sessionId());
        assertEquals(newSession.created(), session.created());
        assertEquals(newSession.userAgent(), session.userAgent());
        assertEquals(newSession.ipAddress(), session.ipAddress());
        assertTrue(newSession.hasUser());

        String encoded = manager.encodeSessionForCookie(newSession);
        Session returned = manager.getOrCreateSession(GET, new DefaultCookie("name", encoded));
        assertEquals(newSession, returned);
        assertTrue(returned.hasUser());
    }

    @Test
    public void addUserOrDie_with_user_throws() {
        Session session = manager.createNewSession(GET);
        Session newSession = manager.addUserOrDie(session, DUMMY_USER);
        assertTrue(newSession.hasUser());

        assertThrows(AssertionError.class, () -> manager.addUserOrDie(newSession, DUMMY_USER));
    }

    private void assertEncodeDecode(long id, int expectedLength) {
        String encoded = manager.encodeSessionId(id);
        assertEquals(expectedLength, encoded.length());
        long sessionId = manager.decodeSessionId(encoded);
        assertEquals(id, sessionId);
    }
}
