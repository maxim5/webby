package io.webby.auth.session;

import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.webby.testing.TestingModels.newSession;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionInterceptorTest {
    @Test
    public void shouldRefresh_created_just_now() {
        Session session = newSession(123, Instant.now());
        assertTrue(SessionInterceptor.shouldRefresh(session));
    }

    @Test
    public void shouldRefresh_created_few_seconds_ago() {
        Session session = newSession(123, Instant.now().minus(5, ChronoUnit.SECONDS));
        assertTrue(SessionInterceptor.shouldRefresh(session));
    }

    @Test
    public void shouldRefresh_created_minutes_ago() {
        Session session = newSession(123, Instant.now().minus(10, ChronoUnit.MINUTES));
        assertFalse(SessionInterceptor.shouldRefresh(session));
    }
}
