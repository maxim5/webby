package io.spbx.util.guava;

import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import static io.spbx.util.guava.SourceCodeEscapers.javaCharEscaper;
import static io.spbx.util.guava.SourceCodeEscapers.javaStringUnicodeEscaper;
import static org.junit.jupiter.api.Assertions.assertThrows;

// https://chromium.googlesource.com/external/guava-libraries/+/484ed1848fc76d4541eacf605100e68018db5af3/
//   guava-tests/test/com/google/common/escape/SourceCodeEscapersTest.java
public class SourceCodeEscapersTest {
    // ASCII control characters.
    private static final String ASCII_CONTROL_UNESCAPED =
        "\000\001\002\003\004\005\006\007" +
        "\010\011\012\013\014\015\016\017" +
        "\020\021\022\023\024\025\026\027" +
        "\030\031\032\033\034\035\036\037";
    private static final String ASCII_CONTROL_ESCAPED =
        "\\u0000\\u0001\\u0002\\u0003\\u0004\\u0005\\u0006\\u0007" +
        "\\b\\t\\n\\u000b\\f\\r\\u000e\\u000f" +
        "\\u0010\\u0011\\u0012\\u0013\\u0014\\u0015\\u0016\\u0017" +
        "\\u0018\\u0019\\u001a\\u001b\\u001c\\u001d\\u001e\\u001f";
    private static final String ASCII_CONTROL_ESCAPED_WITH_OCTAL =
        "\\000\\001\\002\\003\\004\\005\\006\\007" +
        "\\b\\t\\n\\013\\f\\r\\016\\017" +
        "\\020\\021\\022\\023\\024\\025\\026\\027" +
        "\\030\\031\\032\\033\\034\\035\\036\\037";
    // This does not include single quotes, double quotes or backslash.
    private static final String SAFE_ASCII =
        " !#$%&()*+,-./0123456789:;<=>?@" +
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`" +
        "abcdefghijklmnopqrstuvwxyz{|}~";
    private static final String ABOVE_ASCII_UNESCAPED =
        "\200\377\u0100\u0800\u1000\u89AB\uCDEF\uFFFF";
    private static final String ABOVE_ASCII_ESCAPED =
        "\\u0080\\u00ff\\u0100\\u0800\\u1000\\u89ab\\ucdef\\uffff";
    private static final String ABOVE_ASCII_ESCAPED_WITH_OCTAL =
        "\\200\\377\\u0100\\u0800\\u1000\\u89ab\\ucdef\\uffff";

    @Test
    public void testJavaCharEscaper_ascii() {
        assertThat(javaCharEscaper().escape(ASCII_CONTROL_UNESCAPED)).isEqualTo(ASCII_CONTROL_ESCAPED);
        assertThat(javaCharEscaper().escape(SAFE_ASCII)).isEqualTo(SAFE_ASCII);
        assertThat(javaCharEscaper().escape(ABOVE_ASCII_UNESCAPED)).isEqualTo(ABOVE_ASCII_ESCAPED);
        // Single quotes, double quotes and backslash are escaped.
        assertThat(javaCharEscaper().escape("'\"\\")).isEqualTo("\\'\\\"\\\\");
    }

    @Test
    public void testJavaCharEscaper() {
        //noinspection DataFlowIssue
        assertThrows(NullPointerException.class, () -> javaCharEscaper().escape(null));
        assertThat(javaCharEscaper().escape("")).isEqualTo("");

        assertThat(javaCharEscaper().escape("foo")).isEqualTo("foo");
        assertThat(javaCharEscaper().escape("\t")).isEqualTo("\\t");
        assertThat(javaCharEscaper().escape("\n")).isEqualTo("\\n");
        assertThat(javaCharEscaper().escape("\f")).isEqualTo("\\f");
        assertThat(javaCharEscaper().escape("\\")).isEqualTo("\\\\");
        assertThat(javaCharEscaper().escape("'")).isEqualTo("\\'");
        assertThat(javaCharEscaper().escape("\\\b\t\r")).isEqualTo("\\\\\\b\\t\\r");
        assertThat(javaCharEscaper().escape("\u1234")).isEqualTo("\\u1234");
        assertThat(javaCharEscaper().escape("\u0234")).isEqualTo("\\u0234");
        assertThat(javaCharEscaper().escape("\u00ef")).isEqualTo("\\u00ef");
        assertThat(javaCharEscaper().escape("\u0001")).isEqualTo("\\u0001");
        assertThat(javaCharEscaper().escape("\uabcd")).isEqualTo("\\uabcd");

        assertThat(javaCharEscaper().escape("He didn't say, \"stop!\"")).isEqualTo("He didn\\'t say, \\\"stop!\\\"");
        assertThat(javaCharEscaper().escape("This space is non-breaking:\u00a0")).isEqualTo("This space is non-breaking:\\u00a0");
        assertThat(javaCharEscaper().escape("\uABCD\u1234\u012C")).isEqualTo("\\uabcd\\u1234\\u012c");
        assertThat(javaCharEscaper().escape("\ud83d\udc80\ud83d\udd14")).isEqualTo("\\ud83d\\udc80\\ud83d\\udd14");
        assertThat(javaCharEscaper().escape("String with a slash (/) in it")).isEqualTo("String with a slash (/) in it");
    }

    @Test
    public void testJavaStringUnicodeEscaper() {
        // Test that 7-bit ASCII is never escaped.
        assertThat(javaStringUnicodeEscaper().escape("\u0000")).isEqualTo("\u0000");
        assertThat(javaStringUnicodeEscaper().escape("a")).isEqualTo("a");
        assertThat(javaStringUnicodeEscaper().escape("\'")).isEqualTo("\'");
        assertThat(javaStringUnicodeEscaper().escape("\"")).isEqualTo("\"");
        assertThat(javaStringUnicodeEscaper().escape("~")).isEqualTo("~");
        assertThat(javaStringUnicodeEscaper().escape("\u007f")).isEqualTo("\u007f");
        // Test hex escaping for UTF-16.
        assertThat(javaStringUnicodeEscaper().escape("\u0080")).isEqualTo("\\u0080");
        assertThat(javaStringUnicodeEscaper().escape("\u00bb")).isEqualTo("\\u00bb");
        assertThat(javaStringUnicodeEscaper().escape("\u0100")).isEqualTo("\\u0100");
        assertThat(javaStringUnicodeEscaper().escape("\u1000")).isEqualTo("\\u1000");
        assertThat(javaStringUnicodeEscaper().escape("\uffff")).isEqualTo("\\uffff");
        // Make sure HEX_DIGITS are all correct.
        assertThat(javaStringUnicodeEscaper().escape("\u0123")).isEqualTo("\\u0123");
        assertThat(javaStringUnicodeEscaper().escape("\u4567")).isEqualTo("\\u4567");
        assertThat(javaStringUnicodeEscaper().escape("\u89ab")).isEqualTo("\\u89ab");
        assertThat(javaStringUnicodeEscaper().escape("\ucdef")).isEqualTo("\\ucdef");
        // Verify that input is treated as UTF-16 and _not_ Unicode.
        assertThat(javaStringUnicodeEscaper().escape("\uDBFF\uDFFF")).isEqualTo("\\udbff\\udfff");
        // Verify the escaper does _not_ double escape (it is idempotent).
        assertThat(javaStringUnicodeEscaper().escape("\uABCD")).isEqualTo("\\uabcd");
        assertThat(javaStringUnicodeEscaper().escape(javaStringUnicodeEscaper().escape("\uABCD"))).isEqualTo("\\uabcd");
        // Existing escape sequences are left completely unchanged (including case).
        assertThat(javaStringUnicodeEscaper().escape("\\uAbCd")).isEqualTo("\\uAbCd");
    }
}
