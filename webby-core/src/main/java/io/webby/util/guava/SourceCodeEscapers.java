package io.webby.util.guava;

import com.google.common.escape.ArrayBasedCharEscaper;
import com.google.common.escape.CharEscaper;
import com.google.common.escape.Escaper;

import java.util.HashMap;
import java.util.Map;

// While https://github.com/google/guava/issues/1620 is not fixed,
// a relevant copy of guava/src/com/google/common/escape/SourceCodeEscapers.java
//
// https://chromium.googlesource.com/external/guava-libraries/+/484ed1848fc76d4541eacf605100e68018db5af3/
//   guava/src/com/google/common/escape/SourceCodeEscapers.java
//
// Alternatives:
// https://stackoverflow.com/questions/2406121/how-do-i-escape-a-string-in-java
// https://stackoverflow.com/questions/18898773/java-escape-json-string
//
public final class SourceCodeEscapers {
    // For each xxxEscaper() method, please add links to external reference pages
    // that are considered authoritative for the behavior of that escaper.
    // From: http://en.wikipedia.org/wiki/ASCII#ASCII_printable_characters
    private static final char PRINTABLE_ASCII_MIN = 0x20;  // ' '
    private static final char PRINTABLE_ASCII_MAX = 0x7E;  // '~'
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    /**
     * Returns an {@link Escaper} instance that escapes special characters in a
     * string so it can safely be included in either a Java character literal or
     * string literal. This is the preferred way to escape Java characters for
     * use in String or character literals.
     *
     * <p>See: <a href=
     * "http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#101089"
     * >The Java Language Specification</a> for more details.
     */
    public static CharEscaper javaCharEscaper() {
        return JAVA_CHAR_ESCAPER;
    }

    private static final CharEscaper JAVA_CHAR_ESCAPER;
    private static final CharEscaper JAVA_CHAR_ESCAPER_WITH_OCTAL;
    private static final CharEscaper JAVA_STRING_ESCAPER_WITH_OCTAL;

    static {
        Map<Character, String> javaMap = new HashMap<Character, String>();
        javaMap.put('\b', "\\b");
        javaMap.put('\f', "\\f");
        javaMap.put('\n', "\\n");
        javaMap.put('\r', "\\r");
        javaMap.put('\t', "\\t");
        javaMap.put('\"', "\\\"");
        javaMap.put('\\', "\\\\");
        JAVA_STRING_ESCAPER_WITH_OCTAL = new JavaCharEscaperWithOctal(javaMap);
        // The only difference is that the char escaper also escapes single quotes.
        javaMap.put('\'', "\\'");
        JAVA_CHAR_ESCAPER = new JavaCharEscaper(javaMap);
        JAVA_CHAR_ESCAPER_WITH_OCTAL = new JavaCharEscaperWithOctal(javaMap);
    }

    // This escaper does not produce octal escape sequences. See:
    // http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#101089
    //  "Octal escapes are provided for compatibility with C, but can express
    //   only Unicode values \u0000 through \u00FF, so Unicode escapes are
    //   usually preferred."
    private static class JavaCharEscaper extends ArrayBasedCharEscaper {
        JavaCharEscaper(Map<Character, String> replacements) {
            super(replacements, PRINTABLE_ASCII_MIN, PRINTABLE_ASCII_MAX);
        }

        @Override
        protected char[] escapeUnsafe(char c) {
            return asUnicodeHexEscape(c);
        }
    }

    private static class JavaCharEscaperWithOctal extends ArrayBasedCharEscaper {
        JavaCharEscaperWithOctal(Map<Character, String> replacements) {
            super(replacements, PRINTABLE_ASCII_MIN, PRINTABLE_ASCII_MAX);
        }

        @Override
        protected char[] escapeUnsafe(char c) {
            if (c < 0x100) {
                return asOctalEscape(c);
            } else {
                return asUnicodeHexEscape(c);
            }
        }
    }

    /**
     * Returns a {@link CharEscaper} instance that replaces non-ASCII characters
     * in a string with their Unicode escape sequences ({@code \\uxxxx} where
     * {@code xxxx} is a hex number). Existing escape sequences won't be affected.
     *
     * <p>As existing escape sequences are not re-escaped, this escaper is
     * idempotent. However, this means that there can be no well-defined inverse
     * function for this escaper.
     *
     * <p><b>Note</b></p>: the returned escaper is still a {@code CharEscaper} and
     * will not combine surrogate pairs into a single code point before escaping.
     */
    public static CharEscaper javaStringUnicodeEscaper() {
        return JAVA_STRING_UNICODE_ESCAPER;
    }

    private static final CharEscaper JAVA_STRING_UNICODE_ESCAPER
        = new CharEscaper() {
        @Override
        protected char[] escape(char c) {
            if (c < 0x80) {
                return null;
            }
            return asUnicodeHexEscape(c);
        }
    };

    // Helper for common case of escaping a single char.
    private static char[] asUnicodeHexEscape(char c) {
        // Equivalent to String.format("\\u%04x", (int)c);
        char[] r = new char[6];
        r[0] = '\\';
        r[1] = 'u';
        r[5] = HEX_DIGITS[c & 0xF];
        c >>>= 4;
        r[4] = HEX_DIGITS[c & 0xF];
        c >>>= 4;
        r[3] = HEX_DIGITS[c & 0xF];
        c >>>= 4;
        r[2] = HEX_DIGITS[c & 0xF];
        return r;
    }

    // Helper for backward compatible octal escape sequences (c < 256)
    private static char[] asOctalEscape(char c) {
        char[] r = new char[4];
        r[0] = '\\';
        r[3] = HEX_DIGITS[c & 0x7];
        c >>>= 3;
        r[2] = HEX_DIGITS[c & 0x7];
        c >>>= 3;
        r[1] = HEX_DIGITS[c & 0x3];
        return r;
    }
}
