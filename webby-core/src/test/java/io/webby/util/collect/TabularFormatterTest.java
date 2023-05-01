package io.webby.util.collect;

import org.junit.jupiter.api.Test;

import static io.webby.testing.TestingBasics.array;
import static io.webby.util.collect.TabularFormatter.ASCII_FORMATTER;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TabularFormatterTest {
    @Test
    public void formatIntoTableString_0x0() {
        Tabular<String> tab = new ArrayTabular<>(new String[0][0]);
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("<empty>", table);
    }

    @Test
    public void formatIntoTableString_0x1() {
        Tabular<String> tab = new ArrayTabular<>(new String[0][1]);
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("<empty>", table);
    }

    @Test
    public void formatIntoTableString_1x0() {
        Tabular<String> tab = new ArrayTabular<>(new String[1][0]);
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("<empty>", table);
    }

    @Test
    public void formatIntoTableString_1x1() {
        Tabular<String> tab = ArrayTabular.of(
            array("1")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        -----
        | 1 |
        -----\
        """, table);
    }

    @Test
    public void formatIntoTableString_1x1_empty() {
        Tabular<String> tab = ArrayTabular.of(
            array("")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        ----
        |  |
        ----\
        """, table);
    }

    @Test
    public void formatIntoTableString_1x1_null() {
        Tabular<String> tab = ArrayTabular.of(
            array((String) null)
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        --------
        | null |
        --------\
        """, table);
    }

    @Test
    public void formatIntoTableString_2x1_short_value() {
        Tabular<String> tab = ArrayTabular.of(
            array("foo"),
            array("1")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        -------
        | foo |
        -------
        | 1   |
        -------\
        """, table);
    }

    @Test
    public void formatIntoTableString_2x1_long_value() {
        Tabular<String> tab = ArrayTabular.of(
            array("foo"),
            array("123456789")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        -------------
        | foo       |
        -------------
        | 123456789 |
        -------------\
        """, table);
    }

    @Test
    public void formatIntoTableString_2x1_one_empty_value() {
        Tabular<String> tab = ArrayTabular.of(
            array("foo"),
            array("")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        -------
        | foo |
        -------
        |     |
        -------\
        """, table);
    }

    @Test
    public void formatIntoTableString_2x1_all_empty_values() {
        Tabular<String> tab = ArrayTabular.of(
            array(""),
            array("")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        ----
        |  |
        ----
        |  |
        ----\
        """, table);
    }

    @Test
    public void formatIntoTableString_2x2() {
        Tabular<String> tab = ArrayTabular.of(
            array("foo", "bar"),
            array("1",   "123456")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        ----------------
        | foo | bar    |
        ----------------
        | 1   | 123456 |
        ----------------\
        """, table);
    }

    @Test
    public void formatIntoTableString_2x2_null_and_empty() {
        Tabular<String> tab = ArrayTabular.of(
            array("",   ""),
            array(null, null)
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        ---------------
        |      |      |
        ---------------
        | null | null |
        ---------------\
        """, table);
    }

    @Test
    public void formatIntoTableString_3x1() {
        Tabular<String> tab = ArrayTabular.of(
            array("foo"),
            array("12"),
            array("1234")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        --------
        | foo  |
        --------
        | 12   |
        --------
        | 1234 |
        --------\
        """, table);
    }

    @Test
    public void formatIntoTableString_3x3() {
        Tabular<String> tab = ArrayTabular.of(
            array("foo",  "bar",   "baz"),
            array("12",   "12345", ""),
            array("1234", "123",   "12")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        ----------------------
        | foo  | bar   | baz |
        ----------------------
        | 12   | 12345 |     |
        ----------------------
        | 1234 | 123   | 12  |
        ----------------------\
        """, table);
    }

    @Test
    public void formatIntoTableString_1x1_multiline_small() {
        Tabular<String> tab = ArrayTabular.of(
            array("1\n2")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        -----
        | 1 |
        | 2 |
        -----\
        """, table);
    }

    @Test
    public void formatIntoTableString_1x1_multiline_medium() {
        Tabular<String> tab = ArrayTabular.of(
            array("1\n123\n12")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        -------
        | 1   |
        | 123 |
        | 12  |
        -------\
        """, table);
    }

    @Test
    public void formatIntoTableString_1x1_multiline_large() {
        Tabular<String> tab = ArrayTabular.of(
            array("""
                  1234
                  
                  1
                  123
                  123456789""")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        -------------
        | 1234      |
        |           |
        | 1         |
        | 123       |
        | 123456789 |
        -------------\
        """, table);
    }

    @Test
    public void formatIntoTableString_1x1_multiline_all_empty_lines() {
        Tabular<String> tab = ArrayTabular.of(
            array("\n\n")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        ----
        |  |
        |  |
        ----\
        """, table);
    }

    @Test
    public void formatIntoTableString_1x1_multiline_some_empty_lines() {
        Tabular<String> tab = ArrayTabular.of(
            array("\nx\n\n")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        -----
        |   |
        | x |
        |   |
        -----\
        """, table);
    }

    @Test
    public void formatIntoTableString_2x2_multiline_empty_column() {
        Tabular<String> tab = ArrayTabular.of(
            array("foo\nbar", ""),
            array("baz",   "\n\n\n")
        );
        String table = ASCII_FORMATTER.formatIntoTableString(tab);
        assertEquals("""
        ----------
        | foo |  |
        | bar |  |
        ----------
        | baz |  |
        |     |  |
        |     |  |
        ----------\
        """, table);
    }

    @Test
    public void formatIntoTableString_1x1_custom_padding() {
        Tabular<String> tab = ArrayTabular.of(
            array("123")
        );
        String table = TabularFormatter.of('|', '-', 2).formatIntoTableString(tab);
        assertEquals("""
        ---------
        |  123  |
        ---------\
        """, table);
    }
}
