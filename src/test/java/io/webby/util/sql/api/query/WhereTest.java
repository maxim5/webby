package io.webby.util.sql.api.query;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static io.webby.util.sql.api.query.CompareType.GE;
import static io.webby.util.sql.api.query.CompareType.LE;
import static io.webby.util.sql.api.query.Shortcuts.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WhereTest {
    private static final Column INT_COLUMN = newColumn("int", TermType.NUMBER);
    private static final Column STR_COLUMN = newColumn("str", TermType.STRING);

    @Test
    public void compare_expr() {
        assertEquals("WHERE int >= 0", Where.and(GE.compare(INT_COLUMN, num(0))).repr());
        assertEquals("WHERE int >= 0 AND int <= 5", Where.and(GE.compare(INT_COLUMN, num(0)),
                                                              LE.compare(INT_COLUMN, num(5))).repr());
    }

    @Test
    public void like_expr() {
        assertEquals("WHERE str LIKE str", Where.of(like(STR_COLUMN, STR_COLUMN)).repr());
        assertEquals("WHERE str LIKE '%'", Where.of(like(STR_COLUMN, literal("%"))).repr());
        assertEquals("WHERE hex(str) LIKE 'foo%'", Where.of(like(Func.HEX.of(STR_COLUMN), literal("foo%"))).repr());
    }

    private static @NotNull Column newColumn(@NotNull String name, @NotNull TermType type) {
        return new Column() {
            @Override
            public @NotNull String name() {
                return name;
            }
            @Override
            public @NotNull TermType type() {
                return type;
            }
        };
    }
}
