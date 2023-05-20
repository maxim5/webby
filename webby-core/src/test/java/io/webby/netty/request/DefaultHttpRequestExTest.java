package io.webby.netty.request;

import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.webby.auth.session.DefaultSession;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserModel;
import io.webby.netty.HttpConst;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.SessionBuilder;
import io.webby.testing.Testing;
import io.webby.testing.UserBuilder;
import io.webby.util.base.EasyPrimitives.MutableInt;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertJson.assertJsonEquivalent;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultHttpRequestExTest {
    @Test
    public void query_params_simple() {
        HttpRequestEx request = HttpRequestBuilder.get("/?foo=bar").ex();
        assertThat(request.path()).isEqualTo("/");
        assertThat(request.query()).isEqualTo("foo=bar");
        assertThat(request.params().getMap()).isEqualTo(Map.of("foo", List.of("bar")));
    }

    @Test
    public void contentAs_empty() {
        Testing.testStartup();
        HttpRequestEx request = HttpRequestBuilder.post("/").ex();
        assertThat(request.contentAsString()).isEqualTo("");
        assertThrows(IllegalArgumentException.class, () -> request.contentAsJson(Map.class));
        assertThrows(IllegalArgumentException.class, () -> request.contentAsJson(List.class));
        assertThrows(IllegalArgumentException.class, () -> request.contentAsJson(Object.class));
    }

    @Test
    public void contentAs_simple() {
        Testing.testStartup();
        HttpRequestEx request = HttpRequestBuilder.post("/").withContent("{\"value\": 123}").ex();
        assertThat(request.contentAsString()).isEqualTo("{\"value\": 123}");
        Map<?, ?> map = request.contentAsJson(Map.class);
        assertJsonEquivalent(map, Map.of("value", 123));
        MutableInt counter = request.contentAsJson(MutableInt.class);
        assertThat(counter.value).isEqualTo(123);
    }

    @Test
    public void cookies_null() {
        assertThat(HttpRequestBuilder.get("/foo").ex().cookies()).isEmpty();
    }

    @Test
    public void cookies_valid() {
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "foo=bar").ex().cookies())
            .containsExactly(new DefaultCookie("foo", "bar"));
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "foo=bar;").ex().cookies())
            .containsExactly(new DefaultCookie("foo", "bar"));
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "foo=bar;baz=foo").ex().cookies())
            .containsExactly(new DefaultCookie("foo", "bar"), new DefaultCookie("baz", "foo"));
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "foo=bar; __name__=123;").ex().cookies())
            .containsExactly(new DefaultCookie("foo", "bar"), new DefaultCookie("__name__", "123"));
    }

    @Test
    public void cookies_empty_value() {
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "foo=").ex().cookies())
            .containsExactly(new DefaultCookie("foo", ""));
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "foo=;").ex().cookies())
            .containsExactly(new DefaultCookie("foo", ""));
    }

    @Test
    public void cookies_invalid() {
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "").ex().cookies()).isEmpty();
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, ";").ex().cookies()).isEmpty();
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, ";;;").ex().cookies()).isEmpty();
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "=").ex().cookies()).isEmpty();
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "===").ex().cookies()).isEmpty();
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "=foo").ex().cookies()).isEmpty();
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "===foo").ex().cookies()).isEmpty();
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "=;").ex().cookies()).isEmpty();
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "===;;;").ex().cookies()).isEmpty();
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "foo").ex().cookies()).isEmpty();
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "foo;").ex().cookies()).isEmpty();
        assertThat(HttpRequestBuilder.get("/foo").withHeader(HttpConst.COOKIE, "foo;;;").ex().cookies()).isEmpty();
    }

    @Test
    public void attrs_not_set() {
        MutableHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        assertThat(request.attr(0)).isNull();
        assertThrows(AssertionError.class, () -> request.attrOrDie(0));
    }

    @Test
    public void attrs_simple_set_not_null() {
        MutableHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        request.setAttr(0, "foo");
        assertThat(request.attr(0)).isEqualTo("foo");
        assertThat(request.<String>attrOrDie(0)).isEqualTo("foo");
    }

    @Test
    public void attrs_simple_set_nullable() {
        MutableHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        request.setNullableAttr(0, null);
        assertThat(request.attr(0)).isNull();
        assertThrows(AssertionError.class, () -> request.attrOrDie(0));
    }

    @Test
    public void attrs_set_twice() {
        MutableHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        request.setAttr(0, "foo");
        assertThrows(AssertionError.class, () -> request.setAttr(0, "foo"));
    }

    @Test
    public void no_session_unauthenticated() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();

        assertThat(request.isAuthenticated()).isFalse();
        assertThat(request.sessionOrNull()).isNull();
        assertThat(request.<UserModel>user()).isNull();
    }

    @Test
    public void set_session_without_user_unauthenticated() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultSession session = SessionBuilder.ofId(123).build();

        request.setSession(session);

        assertThat(request.isAuthenticated()).isFalse();
        assertThat(request.sessionOrNull()).isEqualTo(session);
        assertThat(request.<UserModel>user()).isNull();
    }

    @Test
    public void set_session_with_user_authenticated() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultSession session = SessionBuilder.ofId(123).withUser(UserBuilder.simple(456)).build();

        request.setSession(session);

        assertThat(request.isAuthenticated()).isTrue();
        assertThat(request.sessionOrNull()).isEqualTo(session);
        assertThat(request.<UserModel>user()).isNull();
    }

    @Test
    public void set_user_having_session_with_user_authenticated() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultUser user = UserBuilder.simple(456);
        DefaultSession session = SessionBuilder.ofId(123).withUser(user).build();

        request.setSession(session);
        request.setUser(user);

        assertThat(request.isAuthenticated()).isTrue();
        assertThat(request.sessionOrNull()).isEqualTo(session);
        assertThat(request.<UserModel>user()).isEqualTo(user);
    }

    @Test
    public void set_user_having_no_session_throws() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultUser user = UserBuilder.simple(456);

        assertThrows(AssertionError.class, () -> request.setUser(user));
    }

    @Test
    public void set_user_having_session_without_user_throws() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultSession session = SessionBuilder.ofId(123).build();
        DefaultUser user = UserBuilder.simple(456);

        request.setSession(session);
        assertThrows(AssertionError.class, () -> request.setUser(user));
    }

    @Test
    public void set_user_having_session_with_another_user_throws() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultSession session = SessionBuilder.ofId(123).withUser(UserBuilder.simple(456)).build();

        request.setSession(session);
        assertThrows(AssertionError.class, () -> request.setUser(UserBuilder.simple(789)));
    }

    @Test
    public void authenticate_having_session_without_user_ok() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultSession session = SessionBuilder.ofId(123).build();
        DefaultUser user = UserBuilder.simple(456);

        request.setSession(session);
        request.authenticate(user);

        assertThat(request.isAuthenticated()).isTrue();
        assertThat(request.sessionOrNull()).isEqualTo(session.withUser(user));
        assertThat(request.<UserModel>user()).isEqualTo(user);
    }

    @Test
    public void authenticate_having_no_session_throws() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultUser user = UserBuilder.simple(456);

        assertThrows(AssertionError.class, () -> request.authenticate(user));
    }

    @Test
    public void authenticate_having_session_with_user_throws() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultUser user = UserBuilder.simple(456);
        DefaultSession session = SessionBuilder.ofId(123).withUser(user).build();

        request.setSession(session);
        assertThrows(AssertionError.class, () -> request.authenticate(user));
    }

    @Test
    public void authenticate_having_session_and_user_throws() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultUser user = UserBuilder.simple(456);
        DefaultSession session = SessionBuilder.ofId(123).withUser(user).build();

        request.setSession(session);
        request.setUser(user);
        assertThrows(AssertionError.class, () -> request.authenticate(user));
    }
}
