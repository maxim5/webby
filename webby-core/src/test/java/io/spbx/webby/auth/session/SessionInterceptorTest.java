package io.spbx.webby.auth.session;

import com.google.inject.Injector;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.spbx.webby.netty.HttpConst;
import io.spbx.webby.netty.intercept.attr.Attributes;
import io.spbx.webby.netty.request.DefaultHttpRequestEx;
import io.spbx.webby.testing.HttpRequestBuilder;
import io.spbx.webby.testing.SessionBuilder;
import io.spbx.webby.testing.Testing;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.google.common.truth.Truth.assertThat;

public class SessionInterceptorTest {
    private final Injector injector = Testing.testStartup();
    private final SessionInterceptor interceptor = injector.getInstance(SessionInterceptor.class);
    private final SessionStore sessionStore = injector.getInstance(SessionStore.class);

    @Test
    public void lifecycle_enter_exit_without_cookie() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").ex();
        DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        assertThat(request.attr(Attributes.Session)).isNull();
        assertThat(sessionStore.isEmpty()).isTrue();

        interceptor.enter(request);
        assertThat(request.session().sessionId()).isGreaterThan(0);
        assertThat(request.session().isAuthenticated()).isFalse();
        assertThat(sessionStore.getSessionByIdOrNull(request.session().sessionId())).isEqualTo(request.session());

        interceptor.exit(request, response);
        assertThat(response.headers().get(HttpConst.SET_COOKIE)).isNotNull();
        assertThat(sessionStore.size()).isEqualTo(1);
    }

    @Test
    public void lifecycle_enter_exit_with_invalid_cookie() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").withCookie("foo=bar;").ex();
        DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        assertThat(request.attr(Attributes.Session)).isNull();
        assertThat(sessionStore.isEmpty()).isTrue();

        interceptor.enter(request);
        assertThat(request.session().sessionId()).isGreaterThan(0);
        assertThat(request.session().isAuthenticated()).isFalse();
        assertThat(sessionStore.getSessionByIdOrNull(request.session().sessionId())).isEqualTo(request.session());

        interceptor.exit(request, response);
        assertThat(response.headers().get(HttpConst.SET_COOKIE)).isNotNull();
        assertThat(sessionStore.size()).isEqualTo(1);
    }

    @Test
    public void lifecycle_enter_exit_with_valid_cookie() {
        DefaultHttpRequestEx request1 = HttpRequestBuilder.get("/foo").ex();
        DefaultHttpResponse response1 = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        interceptor.enter(request1);
        interceptor.exit(request1, response1);
        String cookie = response1.headers().get(HttpConst.SET_COOKIE);

        DefaultHttpRequestEx request2 = HttpRequestBuilder.get("/foo").withCookie(cookie).ex();
        DefaultHttpResponse response2 = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        interceptor.enter(request2);
        assertThat(request2.<DefaultSession>session()).isEqualTo(request1.session());

        interceptor.exit(request2, response2);
        assertThat(response2.headers().get(HttpConst.SET_COOKIE)).isEqualTo(cookie);  // may fail due to last second...
        assertThat(sessionStore.size()).isEqualTo(1);
    }

    @Test
    public void shouldRefresh_created_just_now() {
        DefaultSession session = SessionBuilder.ofId(123).createdAt(Instant.now()).build();
        assertThat(SessionInterceptor.shouldRefresh(session)).isTrue();
    }

    @Test
    public void shouldRefresh_created_few_seconds_ago() {
        DefaultSession session = SessionBuilder.ofId(123).createdAt(Instant.now().minus(5, ChronoUnit.SECONDS)).build();
        assertThat(SessionInterceptor.shouldRefresh(session)).isTrue();
    }

    @Test
    public void shouldRefresh_created_minutes_ago() {
        DefaultSession session = SessionBuilder.ofId(123).createdAt(Instant.now().minus(10, ChronoUnit.MINUTES)).build();
        assertThat(SessionInterceptor.shouldRefresh(session)).isFalse();
    }
}
