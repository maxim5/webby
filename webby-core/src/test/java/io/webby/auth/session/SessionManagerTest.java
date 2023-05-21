package io.webby.auth.session;

import io.webby.testing.Testing;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SessionManagerTest {
    private final SessionManager manager = Testing.testStartup().getInstance(SessionManager.class);

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
        SessionManager manager = Testing.testStartup().getInstance(SessionManager.class);
        assertThrows(RuntimeException.class, () -> manager.decodeSessionId(""));
        assertThrows(RuntimeException.class, () -> manager.decodeSessionId("foo"));
        assertThrows(RuntimeException.class, () -> manager.decodeSessionId("12345"));
    }

    private void assertEncodeDecode(long id, int expectedLength) {
        String encoded = manager.encodeSessionId(id);
        assertThat(encoded.length()).isEqualTo(expectedLength);
        long sessionId = manager.decodeSessionId(encoded);
        assertThat(sessionId).isEqualTo(id);
    }
}
