package io.webby.netty.request;

import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.webby.auth.session.DefaultSession;
import io.webby.auth.user.DefaultUser;
import io.webby.auth.user.UserModel;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.Testing;
import io.webby.testing.TestingModels;
import io.webby.util.base.EasyPrimitives.MutableInt;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.testing.AssertJson.assertJsonEquivalent;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultHttpRequestExTest {
    @Test
    public void query_params_simple() {
        HttpRequestEx request = HttpRequestBuilder.get("/?foo=bar").ex();
        assertEquals("/", request.path());
        assertEquals("foo=bar", request.query());
        assertEquals(Map.of("foo", List.of("bar")), request.params().getMap());
    }

    @Test
    public void contentAs_empty() {
        Testing.testStartup();
        HttpRequestEx request = HttpRequestBuilder.post("/").ex();
        assertEquals("", request.contentAsString());
        assertThrows(IllegalArgumentException.class, () -> request.contentAsJson(Map.class));
        assertThrows(IllegalArgumentException.class, () -> request.contentAsJson(List.class));
        assertThrows(IllegalArgumentException.class, () -> request.contentAsJson(Object.class));
    }

    @Test
    public void contentAs_simple() {
        Testing.testStartup();
        HttpRequestEx request = HttpRequestBuilder.post("/").withContent("{\"value\": 123}").ex();
        assertEquals("{\"value\": 123}", request.contentAsString());
        Map<?, ?> map = request.contentAsJson(Map.class);
        assertJsonEquivalent(map, Map.of("value", 123));
        MutableInt counter = request.contentAsJson(MutableInt.class);
        assertEquals(123, counter.value);
    }

    @Test
    public void cookies_null() {
        HttpRequestEx request = HttpRequestBuilder.get("/?foo=bar").ex();
        assertEquals(List.of(), request.cookies());
    }

    @Test
    public void cookies_simple() {
        HttpRequestEx request = HttpRequestBuilder.get("/?foo=bar").withHeader("Cookie", "foo=bar; __name__=123;").ex();
        assertEquals(List.of(new DefaultCookie("foo", "bar"), new DefaultCookie("__name__", "123")), request.cookies());
    }

    @Test
    public void attrs_not_set() {
        MutableHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        assertNull(request.attr(0));
        assertThrows(AssertionError.class, () -> request.attrOrDie(0));
    }

    @Test
    public void attrs_simple_set_not_null() {
        MutableHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        request.setAttr(0, "foo");
        assertEquals(request.attr(0), "foo");
        assertEquals(request.attrOrDie(0), "foo");
    }

    @Test
    public void attrs_simple_set_nullable() {
        MutableHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        request.setNullableAttr(0, null);
        assertNull(request.attr(0));
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
        DefaultSession session = TestingModels.newSession(123);

        request.setSession(session);

        assertThat(request.isAuthenticated()).isFalse();
        assertThat(request.sessionOrNull()).isEqualTo(session);
        assertThat(request.<UserModel>user()).isNull();
    }

    @Test
    public void set_session_with_user_authenticated() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultSession session = TestingModels.newSession(123).withUser(TestingModels.newUser(456));

        request.setSession(session);

        assertThat(request.isAuthenticated()).isTrue();
        assertThat(request.sessionOrNull()).isEqualTo(session);
        assertThat(request.<UserModel>user()).isNull();
    }

    @Test
    public void set_user_having_session_with_user_authenticated() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultUser user = TestingModels.newUser(456);
        DefaultSession session = TestingModels.newSession(123).withUser(user);

        request.setSession(session);
        request.setUser(user);

        assertThat(request.isAuthenticated()).isTrue();
        assertThat(request.sessionOrNull()).isEqualTo(session);
        assertThat(request.<UserModel>user()).isEqualTo(user);
    }

    @Test
    public void set_user_having_no_session_throws() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultUser user = TestingModels.newUser(456);

        assertThrows(AssertionError.class, () -> request.setUser(user));
    }

    @Test
    public void set_user_having_session_without_user_throws() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultSession session = TestingModels.newSession(123);
        DefaultUser user = TestingModels.newUser(456);

        request.setSession(session);
        assertThrows(AssertionError.class, () -> request.setUser(user));
    }

    @Test
    public void set_user_having_session_with_another_user_throws() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultSession session = TestingModels.newSession(123).withUser(TestingModels.newUser(456));

        request.setSession(session);
        assertThrows(AssertionError.class, () -> request.setUser(TestingModels.newUser(789)));
    }

    @Test
    public void authenticate_having_session_without_user_ok() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultSession session = TestingModels.newSession(123);
        DefaultUser user = TestingModels.newUser(456);

        request.setSession(session);
        request.authenticate(user);

        assertThat(request.isAuthenticated()).isTrue();
        assertThat(request.sessionOrNull()).isEqualTo(session.withUser(user));
        assertThat(request.<UserModel>user()).isEqualTo(user);
    }

    @Test
    public void authenticate_having_no_session_throws() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultUser user = TestingModels.newUser(456);

        assertThrows(AssertionError.class, () -> request.authenticate(user));
    }

    @Test
    public void authenticate_having_session_with_user_throws() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultUser user = TestingModels.newUser(456);
        DefaultSession session = TestingModels.newSession(123).withUser(user);

        request.setSession(session);
        assertThrows(AssertionError.class, () -> request.authenticate(user));
    }

    @Test
    public void authenticate_having_session_and_user_throws() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/").ex();
        DefaultUser user = TestingModels.newUser(456);
        DefaultSession session = TestingModels.newSession(123).withUser(user);

        request.setSession(session);
        request.setUser(user);
        assertThrows(AssertionError.class, () -> request.authenticate(user));
    }
}
