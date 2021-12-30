package io.webby.examples.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.assert200;

public class AcceptQueryIntegrationTest extends BaseHttpIntegrationTest {
    protected final AcceptQuery handler = testSetup(AcceptQuery.class).initHandler();

    @Test
    public void get_simple() {
        assert200(get("/request/query/simple"), "[/request/query/simple] []");
        assert200(get("/request/query/simple?"), "[/request/query/simple] []");
        assert200(get("/request/query/simple?key=value"), "[/request/query/simple] [key=value]");
        assert200(get("/request/query/simple?key="), "[/request/query/simple] [key=]");
        assert200(get("/request/query/simple?=value"), "[/request/query/simple] [=value]");

        assert200(get("/request/query/simple#"), "[/request/query/simple] []");
        assert200(get("/request/query/simple?#"), "[/request/query/simple] [#]");
        assert200(get("/request/query/simple?#ignore"), "[/request/query/simple] [#ignore]");
        assert200(get("/request/query/simple?key=value#ignore"), "[/request/query/simple] [key=value#ignore]");
    }

    @Test
    public void get_param_simple_empty() {
        assert200(get("/request/query/param/key"), "[null] <[]>");
        assert200(get("/request/query/param/key?"), "[null] <[]>");
        assert200(get("/request/query/param/key?x=y"), "[null] <[]>");
        assert200(get("/request/query/param/key?x=y&y=x"), "[null] <[]>");
        assert200(get("/request/query/param/key?x=key"), "[null] <[]>");
        assert200(get("/request/query/param/key?x=key&y=key"), "[null] <[]>");
    }

    @Test
    public void get_param_simple_one() {
        assert200(get("/request/query/param/key?key="), "[] <[]>");
        assert200(get("/request/query/param/key?key=_"), "[_] <[_]>");
        assert200(get("/request/query/param/key?key=&x=y"), "[] <[]>");

        assert200(get("/request/query/param/key?key=x"), "[x] <[x]>");
        assert200(get("/request/query/param/key?key=x&x=key"), "[x] <[x]>");
        assert200(get("/request/query/param/key?x=key&key=x"), "[x] <[x]>");
        assert200(get("/request/query/param/key?x=key&key=x&"), "[x] <[x]>");
    }

    @Test
    public void get_param_simple_one_case_sensitive() {
        assert200(get("/request/query/param/key?Key=x"), "[null] <[]>");
        assert200(get("/request/query/param/key?KEY=x"), "[null] <[]>");
    }

    @Test
    public void get_param_simple_one_with_hash() {
        assert200(get("/request/query/param/key?key=#"), "[] <[]>");
        assert200(get("/request/query/param/key?key=_#ignore"), "[_] <[_]>");
        assert200(get("/request/query/param/key?key=&x=y#key=value"), "[] <[]>");
    }

    @Test
    public void get_param_simple_multiple() {
        assert200(get("/request/query/param/key?key=&key="), "[] <[, ]>");
        assert200(get("/request/query/param/key?key=_&key=_"), "[_] <[_, _]>");
        assert200(get("/request/query/param/key?key=a&key=b"), "[a] <[a, b]>");
        assert200(get("/request/query/param/key?key=b&key=a"), "[b] <[b, a]>");
        assert200(get("/request/query/param/key?key=a&key="), "[a] <[a, ]>");
        assert200(get("/request/query/param/key?key=&key=a"), "[] <[, a]>");
    }
}
