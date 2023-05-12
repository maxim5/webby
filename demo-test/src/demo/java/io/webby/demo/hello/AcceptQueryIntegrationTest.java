package io.webby.demo.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.assertThat;

public class AcceptQueryIntegrationTest extends BaseHttpIntegrationTest {
    protected final AcceptQuery handler = testSetup(AcceptQuery.class).initHandler();

    @Test
    public void get_simple() {
        assertThat(get("/request/query/simple")).is200().hasContent("[/request/query/simple] []");
        assertThat(get("/request/query/simple?")).is200().hasContent("[/request/query/simple] []");
        assertThat(get("/request/query/simple?key=value")).is200().hasContent("[/request/query/simple] [key=value]");
        assertThat(get("/request/query/simple?key=")).is200().hasContent("[/request/query/simple] [key=]");
        assertThat(get("/request/query/simple?=value")).is200().hasContent("[/request/query/simple] [=value]");

        assertThat(get("/request/query/simple#")).is200().hasContent("[/request/query/simple] []");
        assertThat(get("/request/query/simple?#")).is200().hasContent("[/request/query/simple] [#]");
        assertThat(get("/request/query/simple?#ignore")).is200().hasContent("[/request/query/simple] [#ignore]");
        assertThat(get("/request/query/simple?key=value#ignore")).is200().hasContent("[/request/query/simple] [key=value#ignore]");
    }

    @Test
    public void get_param_simple_empty() {
        assertThat(get("/request/query/param/key")).is200().hasContent("[null] <[]>");
        assertThat(get("/request/query/param/key?")).is200().hasContent("[null] <[]>");
        assertThat(get("/request/query/param/key?x=y")).is200().hasContent("[null] <[]>");
        assertThat(get("/request/query/param/key?x=y&y=x")).is200().hasContent("[null] <[]>");
        assertThat(get("/request/query/param/key?x=key")).is200().hasContent("[null] <[]>");
        assertThat(get("/request/query/param/key?x=key&y=key")).is200().hasContent("[null] <[]>");
    }

    @Test
    public void get_param_simple_one() {
        assertThat(get("/request/query/param/key?key=")).is200().hasContent("[] <[]>");
        assertThat(get("/request/query/param/key?key=_")).is200().hasContent("[_] <[_]>");
        assertThat(get("/request/query/param/key?key=&x=y")).is200().hasContent("[] <[]>");

        assertThat(get("/request/query/param/key?key=x")).is200().hasContent("[x] <[x]>");
        assertThat(get("/request/query/param/key?key=x&x=key")).is200().hasContent("[x] <[x]>");
        assertThat(get("/request/query/param/key?x=key&key=x")).is200().hasContent("[x] <[x]>");
        assertThat(get("/request/query/param/key?x=key&key=x&")).is200().hasContent("[x] <[x]>");
    }

    @Test
    public void get_param_simple_one_case_sensitive() {
        assertThat(get("/request/query/param/key?Key=x")).is200().hasContent("[null] <[]>");
        assertThat(get("/request/query/param/key?KEY=x")).is200().hasContent("[null] <[]>");
    }

    @Test
    public void get_param_simple_one_with_hash() {
        assertThat(get("/request/query/param/key?key=#")).is200().hasContent("[] <[]>");
        assertThat(get("/request/query/param/key?key=_#ignore")).is200().hasContent("[_] <[_]>");
        assertThat(get("/request/query/param/key?key=&x=y#key=value")).is200().hasContent("[] <[]>");
    }

    @Test
    public void get_param_simple_multiple() {
        assertThat(get("/request/query/param/key?key=&key=")).is200().hasContent("[] <[, ]>");
        assertThat(get("/request/query/param/key?key=_&key=_")).is200().hasContent("[_] <[_, _]>");
        assertThat(get("/request/query/param/key?key=a&key=b")).is200().hasContent("[a] <[a, b]>");
        assertThat(get("/request/query/param/key?key=b&key=a")).is200().hasContent("[b] <[b, a]>");
        assertThat(get("/request/query/param/key?key=a&key=")).is200().hasContent("[a] <[a, ]>");
        assertThat(get("/request/query/param/key?key=&key=a")).is200().hasContent("[] <[, a]>");
    }
}
