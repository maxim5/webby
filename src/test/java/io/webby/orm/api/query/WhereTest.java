package io.webby.orm.api.query;

import org.junit.jupiter.api.Test;

import static io.webby.orm.api.query.AssertSql.assertRepr;
import static io.webby.orm.api.query.CompareType.GE;
import static io.webby.orm.api.query.CompareType.LE;
import static io.webby.orm.api.query.Shortcuts.*;

public class WhereTest {
    @Test
    public void compare_expr() {
        assertRepr(Where.and(GE.compare(FakeColumn.INT, num(0))), "WHERE i >= 0");
        assertRepr(Where.and(GE.compare(FakeColumn.INT, num(0)),
                             LE.compare(FakeColumn.INT, num(5))),
                   "WHERE i >= 0 AND i <= 5");
    }

    @Test
    public void like_expr() {
        assertRepr(Where.of(like(FakeColumn.STR, FakeColumn.FOO)), "WHERE s LIKE foo");
        assertRepr(Where.of(like(FakeColumn.STR, literal("%"))), "WHERE s LIKE '%'");
        assertRepr(Where.of(like(Func.HEX.apply(FakeColumn.STR), literal("foo%"))), "WHERE hex(s) LIKE 'foo%'");
    }
}
