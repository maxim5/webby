package io.webby.util.sql;

import io.webby.util.sql.api.SqlDebug;
import io.webby.util.sql.api.SqlDebug.Row;
import io.webby.util.sql.api.SqlDebug.RowValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

// TODO: Support multi-line values?
@SuppressWarnings("UnnecessaryStringEscape")
public class SqlDebugTest {
    @Test
    public void formatIntoTableString_one_cell_short_value() {
        List<Row> rows = List.of(
            row(value("foo", "1"))
        );
        String table = SqlDebug.FORMATTER.formatIntoTableString(rows);
        Assertions.assertEquals("""
         foo \n\
        -----
         1   \
        """, table);
    }

    @Test
    public void formatIntoTableString_one_cell_long_value() {
        List<Row> rows = List.of(
            row(value("foo", "123456789"))
        );
        String table = SqlDebug.FORMATTER.formatIntoTableString(rows);
        Assertions.assertEquals("""
         foo       \n\
        -----------
         123456789 \
        """, table);
    }

    @Test
    public void formatIntoTableString_one_cell_empty_value() {
        List<Row> rows = List.of(
            row(value("foo", ""))
        );
        String table = SqlDebug.FORMATTER.formatIntoTableString(rows);
        Assertions.assertEquals("""
         foo \n\
        -----
             \
        """, table);
    }

    @Test
    public void formatIntoTableString_one_row() {
        List<Row> rows = List.of(
            row(value("foo", "1"), value("bar", "123456"))
        );
        String table = SqlDebug.FORMATTER.formatIntoTableString(rows);
        Assertions.assertEquals("""
         foo | bar    \n\
        --------------
         1   | 123456 \
        """, table);
    }

    @Test
    public void formatIntoTableString_few_rows_one_column() {
        List<Row> rows = List.of(
            row(value("foo", "12")),
            row(value("foo", "1234"))
        );
        String table = SqlDebug.FORMATTER.formatIntoTableString(rows);
        Assertions.assertEquals("""
         foo  \n\
        ------
         12   \n\
         1234 \
        """, table);
    }

    @Test
    public void formatIntoTableString_few_rows_multi_columns() {
        List<Row> rows = List.of(
            row(value("foo", "12"), value("bar", "12345"), value("baz", "")),
            row(value("foo", "1234"), value("bar", "123"), value("baz", "12"))
        );
        String table = SqlDebug.FORMATTER.formatIntoTableString(rows);
        Assertions.assertEquals("""
         foo  | bar   | baz \n\
        --------------------
         12   | 12345 |     \n\
         1234 | 123   | 12  \
        """, table);
    }

    private static @NotNull Row row(@NotNull RowValue ... values) {
        return new Row(List.of(values));
    }

    private static @NotNull RowValue value(String name, String value) {
        return new RowValue(name, value);
    }
}
