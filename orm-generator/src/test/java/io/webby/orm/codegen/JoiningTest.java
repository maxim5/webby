package io.webby.orm.codegen;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.orm.codegen.Indent.INDENT0;
import static io.webby.orm.codegen.Indent.INDENT1;
import static io.webby.orm.codegen.Joining.linesJoiner;

public class JoiningTest {
    @Test
    public void linesJoiner_simple() {
        assertThat(Stream.of("foo", "bar").collect(linesJoiner(INDENT0))).isEqualTo("foo\n" +
                                                                                    "bar");
        assertThat(Stream.of("foo", "bar").collect(linesJoiner(INDENT1))).isEqualTo("    foo\n" +
                                                                                    "    bar");
        assertThat(Stream.of("", "").collect(linesJoiner(INDENT0))).isEqualTo("\n");
        assertThat(Stream.of("foo", "", "bar").collect(linesJoiner(INDENT1))).isEqualTo("    foo\n" +
                                                                                        "    \n" +
                                                                                        "    bar");
    }

    @Test
    public void linesJoiner_filter_empty() {
        assertThat(Stream.of("", "").collect(linesJoiner(INDENT0, true))).isEqualTo("");
        assertThat(Stream.of("foo", "", "bar").collect(linesJoiner(INDENT1, true))).isEqualTo("    foo\n" +
                                                                                              "    bar");
        assertThat(Stream.of("foo", "", "bar").collect(linesJoiner(INDENT1, false))).isEqualTo("    foo\n" +
                                                                                               "    \n" +
                                                                                               "    bar");
    }
}
