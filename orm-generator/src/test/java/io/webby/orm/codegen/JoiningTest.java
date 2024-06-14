package io.webby.orm.codegen;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.orm.codegen.Joining.linesJoiner;

public class JoiningTest {
    @Test
    public void linesJoiner_simple() {
        assertThat(Stream.of("foo", "bar").collect(linesJoiner(""))).isEqualTo("foo\n" +
                                                                               "bar");
        assertThat(Stream.of("foo", "bar").collect(linesJoiner("__"))).isEqualTo("__foo\n" +
                                                                                 "__bar");
        assertThat(Stream.of("", "").collect(linesJoiner(""))).isEqualTo("\n");
        assertThat(Stream.of("foo", "", "bar").collect(linesJoiner(" "))).isEqualTo(" foo\n" +
                                                                                    " \n" +
                                                                                    " bar");
    }

    @Test
    public void linesJoiner_filter_empty() {
        assertThat(Stream.of("", "").collect(linesJoiner("", true))).isEqualTo("");
        assertThat(Stream.of("foo", "", "bar").collect(linesJoiner("_", true))).isEqualTo("_foo\n" +
                                                                                          "_bar");
        assertThat(Stream.of("foo", "", "bar").collect(linesJoiner("_", false))).isEqualTo("_foo\n" +
                                                                                           "_\n" +
                                                                                           "_bar");
    }
}
