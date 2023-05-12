package io.webby.demo.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.assertThat;

public class AcceptStringsIntegrationTest extends BaseHttpIntegrationTest {
    protected final AcceptStrings handler = testSetup(AcceptStrings.class).initHandler();

    @Test
    public void get_one_string() {
        assertThat(get("/strings/one_string/10")).is200().hasContent("10");
        assertThat(get("/strings/one_string/foo")).is200().hasContent("FOO");
        assertThat(get("/strings/one_string/_")).is200().hasContent("_");
        assertThat(get("/strings/one_string/foo/")).is404();
    }

    @Test
    public void get_one_array() {
        assertThat(get("/strings/one_array/10")).is200().hasContent("10");
        assertThat(get("/strings/one_array/foo")).is200().hasContent("FOO");
        assertThat(get("/strings/one_array/_")).is200().hasContent("_");
        assertThat(get("/strings/one_array/")).is404();
    }

    @Test
    public void get_one_char_sequence() {
        assertThat(get("/strings/one_char_sequence/10")).is200().hasContent("10");
        assertThat(get("/strings/one_char_sequence/foo")).is200().hasContent("FOO");
        assertThat(get("/strings/one_char_sequence/_")).is200().hasContent("_");
        assertThat(get("/strings/one_char_sequence/")).is404();
    }

    @Test
    public void get_two_strings() {
        assertThat(get("/strings/two_strings/10/20")).is200().hasContent("10-20");
        assertThat(get("/strings/two_strings/foo/bar")).is200().hasContent("foo-bar");
        assertThat(get("/strings/two_strings/Foo/Bar")).is200().hasContent("Foo-Bar");
        assertThat(get("/strings/two_strings/_/_")).is200().hasContent("_-_");
        assertThat(get("/strings/two_strings//_")).is404();
        assertThat(get("/strings/two_strings/foo/")).is404();
    }

    @Test
    public void get_two_arrays() {
        assertThat(get("/strings/two_arrays/10/20")).is200().hasContent("10.20");
        assertThat(get("/strings/two_arrays/foo/bar")).is200().hasContent("foo.bar");
        assertThat(get("/strings/two_arrays/Foo/Bar")).is200().hasContent("Foo.Bar");
        assertThat(get("/strings/two_arrays/_/_")).is200().hasContent("_._");
        assertThat(get("/strings/two_arrays//_")).is404();
        assertThat(get("/strings/two_arrays/foo/")).is404();
    }

    @Test
    public void get_two_char_sequences() {
        assertThat(get("/strings/two_char_sequences/10/20")).is200().hasContent("10*20");
        assertThat(get("/strings/two_char_sequences/foo/bar")).is200().hasContent("foo*bar");
        assertThat(get("/strings/two_char_sequences/Foo/Bar")).is200().hasContent("Foo*Bar");
        assertThat(get("/strings/two_char_sequences/_/_")).is200().hasContent("_*_");
        assertThat(get("/strings/two_char_sequences//_")).is404();
        assertThat(get("/strings/two_char_sequences/foo/")).is404();
    }

    @Test
    public void get_two_string_and_array() {
        assertThat(get("/strings/two_string_and_array/10/20")).is200().hasContent("10:20");
        assertThat(get("/strings/two_string_and_array/foo/bar")).is200().hasContent("foo:bar");
        assertThat(get("/strings/two_string_and_array/Foo/Bar")).is200().hasContent("Foo:Bar");
        assertThat(get("/strings/two_string_and_array/_/_")).is200().hasContent("_:_");
        assertThat(get("/strings/two_string_and_array//_")).is404();
        assertThat(get("/strings/two_string_and_array/foo/")).is404();
    }
}
