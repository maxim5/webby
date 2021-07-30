package io.webby.auth.session;

import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.webby.Testing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertThrows(RuntimeException.class, () -> manager.decodeSessionId(""));
        Assertions.assertThrows(RuntimeException.class, () -> manager.decodeSessionId("foo"));
        Assertions.assertThrows(RuntimeException.class, () -> manager.decodeSessionId("12345"));
    }

    @Test
    public void getOrCreateSession_null() {
        Session session = manager.getOrCreateSession(null);
        Assertions.assertTrue(session.shouldRefresh());
    }

    @Test
    public void getOrCreateSession_invalid_cookie() {
        Session session = manager.getOrCreateSession(new DefaultCookie("name", "foo"));
        Assertions.assertTrue(session.shouldRefresh());
    }

    @Test
    public void getOrCreateSession_valid_cookie() {
        Session session = manager.createNewSession();
        String encoded = manager.encodeSession(session);
        Session returned = manager.getOrCreateSession(new DefaultCookie("name", encoded));
        Assertions.assertEquals(session, returned);
    }

    private void assertEncodeDecode(long id, int expectedLength) {
        String encoded = manager.encodeSessionId(id);
        Assertions.assertEquals(expectedLength, encoded.length());
        long sessionId = manager.decodeSessionId(encoded);
        Assertions.assertEquals(id, sessionId);
    }
}
