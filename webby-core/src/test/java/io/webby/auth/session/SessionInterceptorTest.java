package io.webby.auth.session;

import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.webby.netty.HttpConst;
import io.webby.netty.intercept.attr.Attributes;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.SessionBuilder;
import io.webby.testing.Testing;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionInterceptorTest {
    private final SessionInterceptor interceptor = Testing.testStartup().getInstance(SessionInterceptor.class);

    @Test
    public void lifecycle_enter_exit_without_cookies() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").ex();
        DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        assertThat(request.attr(Attributes.Session)).isNull();

        interceptor.enter(request);
        assertThat(request.session().sessionId()).isGreaterThan(0);
        assertThat(request.session().isAuthenticated()).isFalse();

        interceptor.exit(request, response);
        assertThat(response.headers().get(HttpConst.SET_COOKIE)).isNotNull();
    }

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
