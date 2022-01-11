package io.webby.demo.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.*;

public class AcceptStringsIntegrationTest extends BaseHttpIntegrationTest {
    protected final AcceptStrings handler = testSetup(AcceptStrings.class).initHandler();

    @Test
    public void get_one_string() {
        assert200(get("/strings/one_string/10"), "10");
        assert200(get("/strings/one_string/foo"), "FOO");
        assert200(get("/strings/one_string/_"), "_");
        assert404(get("/strings/one_string/foo/"));
    }

    @Test
    public void get_one_array() {
        assert200(get("/strings/one_array/10"), "10");
        assert200(get("/strings/one_array/foo"), "FOO");
        assert200(get("/strings/one_array/_"), "_");
        assert404(get("/strings/one_array/"));
    }

    @Test
    public void get_one_char_sequence() {
        assert200(get("/strings/one_char_sequence/10"), "10");
        assert200(get("/strings/one_char_sequence/foo"), "FOO");
        assert200(get("/strings/one_char_sequence/_"), "_");
        assert404(get("/strings/one_char_sequence/"));
    }

    @Test
    public void get_two_strings() {
        assert200(get("/strings/two_strings/10/20"), "10-20");
        assert200(get("/strings/two_strings/foo/bar"), "foo-bar");
        assert200(get("/strings/two_strings/Foo/Bar"), "Foo-Bar");
        assert200(get("/strings/two_strings/_/_"), "_-_");
        assert404(get("/strings/two_strings//_"));
        assert404(get("/strings/two_strings/foo/"));
    }

    @Test
    public void get_two_arrays() {
        assert200(get("/strings/two_arrays/10/20"), "10.20");
        assert200(get("/strings/two_arrays/foo/bar"), "foo.bar");
        assert200(get("/strings/two_arrays/Foo/Bar"), "Foo.Bar");
        assert200(get("/strings/two_arrays/_/_"), "_._");
        assert404(get("/strings/two_arrays//_"));
        assert404(get("/strings/two_arrays/foo/"));
    }

    @Test
    public void get_two_char_sequences() {
        assert200(get("/strings/two_char_sequences/10/20"), "10*20");
        assert200(get("/strings/two_char_sequences/foo/bar"), "foo*bar");
        assert200(get("/strings/two_char_sequences/Foo/Bar"), "Foo*Bar");
        assert200(get("/strings/two_char_sequences/_/_"), "_*_");
        assert404(get("/strings/two_char_sequences//_"));
        assert404(get("/strings/two_char_sequences/foo/"));
    }

    @Test
    public void get_two_string_and_array() {
        assert200(get("/strings/two_string_and_array/10/20"), "10:20");
        assert200(get("/strings/two_string_and_array/foo/bar"), "foo:bar");
        assert200(get("/strings/two_string_and_array/Foo/Bar"), "Foo:Bar");
        assert200(get("/strings/two_string_and_array/_/_"), "_:_");
        assert404(get("/strings/two_string_and_array//_"));
        assert404(get("/strings/two_string_and_array/foo/"));
    }
}
