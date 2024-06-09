package io.webby.orm.codegen;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.webby.testing.orm.AssertCode;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static io.webby.orm.codegen.JavaSupport.wrapAsStringLiteral;


public class JavaSupportTest {
    @Test
    public void wrapAsStringLiteral_one_line() {
        assertThatText("").wrappedAsLiteralEqualsTo("\"\"");
        assertThatText(" ").wrappedAsLiteralEqualsTo("\" \"");
        assertThatText("foo").wrappedAsLiteralEqualsTo("\"foo\"");
        assertThatText("foo bar").wrappedAsLiteralEqualsTo("\"foo bar\"");
        assertThatText("'foo").wrappedAsLiteralEqualsTo("\"\\'foo\"");
        assertThatText("\"foo").wrappedAsLiteralEqualsTo("\"\\\"foo\"");
        assertThatText("\t").wrappedAsLiteralEqualsTo("\"\\t\"");
    }

    @Test
    public void wrapAsStringLiteral_text_block() {
        assertThatText("""
                       
                       """
        ).wrappedAsLiteralEqualsTo("""
                                   \"\"\"
                                   
                                   \"\"\"\
                                   """);

        assertThatText("""
                       foo
                       bar
                       """
        ).wrappedAsLiteralEqualsTo("""
                                   \"\"\"
                                   foo
                                   bar
                                   \"\"\"\
                                   """);

        assertThatText("""
                       foo
                       bar\
                       """
        ).wrappedAsLiteralEqualsTo("""
                                   \"\"\"
                                   foo
                                   bar
                                   \"\"\"\
                                   """);

        assertThatText("""
                       "foo"
                       'bar'\
                       """
        ).wrappedAsLiteralEqualsTo("""
                                   \"\"\"
                                   \\"foo\\"
                                   \\'bar\\'
                                   \"\"\"\
                                   """);
    }

    @CheckReturnValue
    private static @NotNull StringLiteralSubject assertThatText(@NotNull String text) {
        return new StringLiteralSubject(text);
    }

    @CanIgnoreReturnValue
    private record StringLiteralSubject(@NotNull String text) {
        public @NotNull StringLiteralSubject wrappedAsLiteralEqualsTo(@NotNull String expected) {
            AssertCode.assertThatJava(wrapAsStringLiteral(text)).isEqualTo(expected);
            AssertSnippet.assertThatJava(wrapAsStringLiteral(new Snippet().withMultiline(text))).isEqualTo(expected);
            return this;
        }
    }
}
