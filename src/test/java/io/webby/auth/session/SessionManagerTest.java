package io.webby.auth.session;

import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.webby.testing.Testing;
import org.junit.jupiter.api.Test;

import static io.webby.testing.FakeRequests.getEx;
import static org.junit.jupiter.api.Assertions.*;

public class SessionManagerTest {
    private final SessionManager manager = Testing.testStartupNoHandlers().getInstance(SessionManager.class);

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
        Session session = manager.getOrCreateSession(getEx("/"), null);
        assertTrue(session.shouldRefresh());
    }

    @Test
    public void getOrCreateSession_invalid_cookie() {
        Session session = manager.getOrCreateSession(getEx("/"), new DefaultCookie("name", "foo"));
        assertTrue(session.shouldRefresh());
    }

    @Test
    public void getOrCreateSession_valid_cookie() {
        Session session = manager.createNewSession(getEx("/"));
        String encoded = manager.encodeSessionForCookie(session);
        Session returned = manager.getOrCreateSession(getEx("/"), new DefaultCookie("name", encoded));
        assertEquals(session, returned);
    }

    private void assertEncodeDecode(long id, int expectedLength) {
        String encoded = manager.encodeSessionId(id);
        assertEquals(expectedLength, encoded.length());
        long sessionId = manager.decodeSessionId(encoded);
        assertEquals(id, sessionId);
    }
}
