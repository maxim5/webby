package io.webby.auth.session;

import io.webby.testing.SessionBuilder;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionInterceptorTest {
    @Test
    public void shouldRefresh_created_just_now() {
        DefaultSession session = SessionBuilder.ofId(123).createdAt(Instant.now()).build();
        assertTrue(SessionInterceptor.shouldRefresh(session));
    }

    @Test
    public void shouldRefresh_created_few_seconds_ago() {
        DefaultSession session = SessionBuilder.ofId(123).createdAt(Instant.now().minus(5, ChronoUnit.SECONDS)).build();
        assertTrue(SessionInterceptor.shouldRefresh(session));
    }

    @Test
    public void shouldRefresh_created_minutes_ago() {
        DefaultSession session = SessionBuilder.ofId(123).createdAt(Instant.now().minus(10, ChronoUnit.MINUTES)).build();
        assertFalse(SessionInterceptor.shouldRefresh(session));
    }
}
