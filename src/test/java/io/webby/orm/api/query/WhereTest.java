package io.webby.orm.api.query;

import org.junit.jupiter.api.Test;

import static io.webby.orm.api.query.CompareType.GE;
import static io.webby.orm.api.query.CompareType.LE;
import static io.webby.orm.api.query.Shortcuts.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WhereTest {
    @Test
    public void compare_expr() {
        assertEquals("WHERE i >= 0", Where.and(GE.compare(FakeColumn.INT, num(0))).repr());
        assertEquals("WHERE i >= 0 AND i <= 5", Where.and(GE.compare(FakeColumn.INT, num(0)),
                                                          LE.compare(FakeColumn.INT, num(5))).repr());
    }

    @Test
    public void like_expr() {
        assertEquals("WHERE s LIKE foo", Where.of(like(FakeColumn.STR, FakeColumn.FOO)).repr());
        assertEquals("WHERE s LIKE '%'", Where.of(like(FakeColumn.STR, literal("%"))).repr());
        assertEquals("WHERE hex(s) LIKE 'foo%'", Where.of(like(Func.HEX.apply(FakeColumn.STR), literal("foo%"))).repr());
    }
}
