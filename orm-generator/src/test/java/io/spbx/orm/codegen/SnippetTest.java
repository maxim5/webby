package io.spbx.orm.codegen;

import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static io.spbx.util.testing.TestingBasics.array;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class SnippetTest {
    @Test
    public void build_via_withLine() {
        Snippet snippet = new Snippet();
        assertThat(snippet).containsLinesExactly().isNotBlock();
        assertThat(snippet.withLine("foo")).containsLinesExactly("foo").isNotBlock();
        assertThat(snippet.withLine("bar")).containsLinesExactly("foo", "bar").isBlock();
    }

    @Test
    public void build_via_withLine_parts() {
        assertThat(new Snippet().withLine("a", "b")).containsLinesExactly("ab").isNotBlock();
        assertThat(new Snippet().withLine("a", "b", "c")).containsLinesExactly("abc").isNotBlock();
        assertThat(new Snippet().withLine("a", "b", "c", "d")).containsLinesExactly("abcd").isNotBlock();
    }

    @Test
    public void build_via_withFormattedLine() {
        assertThat(new Snippet().withFormattedLine("%s", "foo")).containsLinesExactly("foo").isNotBlock();
        assertThat(new Snippet().withFormattedLine("%s%s", "foo", "bar")).containsLinesExactly("foobar").isNotBlock();
        assertThat(new Snippet().withFormattedLine("%d-%d-%d", 1, 2, 3)).containsLinesExactly("1-2-3").isNotBlock();
    }

    @Test
    public void build_via_withLines() {
        Snippet snippet = new Snippet();
        assertThat(snippet).containsLinesExactly().isNotBlock();
        assertThat(snippet.withLines(List.of("foo"))).containsLinesExactly("foo").isNotBlock();
        assertThat(snippet.withLines(Stream.of("bar"))).containsLinesExactly("foo", "bar").isBlock();
        assertThat(snippet.withLines(array("baz"))).containsLinesExactly("foo", "bar", "baz").isBlock();
    }

    @Test
    public void build_via_withMultiline() {
        Snippet snippet = new Snippet();
        assertThat(snippet).containsLinesExactly().isNotBlock();
        assertThat(snippet.withMultiline("foo")).containsLinesExactly("foo").isNotBlock();
        assertThat(snippet.withMultiline("\n")).containsLinesExactly("foo", "").isBlock();
        assertThat(snippet.withMultiline("bar\n")).containsLinesExactly("foo", "", "bar").isBlock();
    }

    @Test
    public void build_via_withMultiline_block() {
        assertThat(new Snippet().withMultiline("foo\n")).containsLinesExactly("foo").isBlock();
        assertThat(new Snippet().withMultiline("foo\nbar")).containsLinesExactly("foo", "bar").isBlock();
        assertThat(new Snippet().withMultiline("foo\rbar")).containsLinesExactly("foo", "bar").isBlock();
        assertThat(new Snippet().withMultiline("foo\r\nbar")).containsLinesExactly("foo", "bar").isBlock();
    }

    @Test
    public void build_via_withFormattedMultiline() {
        assertThat(new Snippet().withFormattedMultiline("%s", "foo")).containsLinesExactly("foo").isNotBlock();
        assertThat(new Snippet().withFormattedMultiline("%s\n", "foo")).containsLinesExactly("foo").isBlock();
        assertThat(new Snippet().withFormattedMultiline("%d\r\n%d", 1, 2)).containsLinesExactly("1", "2").isBlock();
        assertThat(new Snippet().withFormattedMultiline("\n%d-%d", 1, 2)).containsLinesExactly("", "1-2").isBlock();
    }

    @Test
    public void build_via_withMultilines_block() {
        assertThat(new Snippet().withMultilines(List.of("foo\nbar"))).containsLinesExactly("foo", "bar").isBlock();
        assertThat(new Snippet().withMultilines(Stream.of("foo\nbar"))).containsLinesExactly("foo", "bar").isBlock();
        assertThat(new Snippet().withMultilines(array("foo\nbar"))).containsLinesExactly("foo", "bar").isBlock();
    }

    @Test
    public void build_withForceBlock() {
        Snippet snippet = new Snippet().withLine("foo");
        assertThat(snippet).containsLinesExactly("foo").isNotBlock();
        assertThat(snippet.withForceBlock(true)).containsLinesExactly("foo").isBlock();
        assertThat(snippet.withForceBlock(false)).containsLinesExactly("foo").isBlock();  // remains a block!
    }

    @Test
    public void isolation() {
        Snippet snippet1 = new Snippet();
        assertThat(snippet1).containsLinesExactly().isNotBlock();
        assertThat(snippet1.withLine("foo")).containsLinesExactly("foo").isNotBlock();

        Snippet snippet2 = new Snippet().withLines(snippet1);
        assertThat(snippet2).containsLinesExactly("foo").isNotBlock();

        assertThat(snippet1.withLine("bar")).containsLinesExactly("foo", "bar").isBlock();
        assertThat(snippet2).containsLinesExactly("foo").isNotBlock();
    }

    @Test
    public void build_incorrectly() {
        assertThrows(AssertionError.class, () -> new Snippet().withLine("\n"));
        assertThrows(AssertionError.class, () -> new Snippet().withLine("\r"));
        assertThrows(AssertionError.class, () -> new Snippet().withLine("foo\n\rbar"));
        assertThrows(AssertionError.class, () -> new Snippet().withFormattedLine("foo\n\rbar"));
        assertThrows(AssertionError.class, () -> new Snippet().withLines(List.of("\n")));
        assertThrows(AssertionError.class, () -> new Snippet().withLines(Stream.of("\n")));
        assertThrows(AssertionError.class, () -> new Snippet().withLines(array("\n")));
    }

    @CheckReturnValue
    private static @NotNull SnippetSubject assertThat(@NotNull Snippet snippet) {
        return new SnippetSubject(snippet);
    }

    @CanIgnoreReturnValue
    private record SnippetSubject(@NotNull Snippet snippet) {
        public @NotNull SnippetSubject containsLinesExactly(@NotNull String @NotNull ... lines) {
            Truth.assertThat(snippet.lines()).containsExactlyElementsIn(lines);
            return this;
        }

        public @NotNull SnippetSubject isBlock() {
            return isBlock(true);
        }

        public @NotNull SnippetSubject isNotBlock() {
            return isBlock(false);
        }

        public @NotNull SnippetSubject isBlock(boolean expected) {
            Truth.assertThat(snippet.isBlock()).isEqualTo(expected);
            return this;
        }
    }
}
