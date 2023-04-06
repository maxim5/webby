package io.webby.orm.api.query;

import io.webby.orm.testing.FakeColumn;
import org.junit.jupiter.api.Test;

import static io.webby.orm.api.query.CompareType.GE;
import static io.webby.orm.api.query.CompareType.LE;
import static io.webby.orm.api.query.Shortcuts.*;
import static io.webby.orm.testing.AssertSql.assertArgs;
import static io.webby.orm.testing.AssertSql.assertRepr;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    public void lookup_variable() {
        Where where = Where.of(lookupBy(FakeColumn.INT, var(1)));
        assertRepr(where, "WHERE i = ?");
        assertArgs(where, 1);
        assertTrue(where.args().isAllResolved());
    }

    @Test
    public void lookup_variables() {
        Where where = Where.of(lookupBy(FakeColumn.INT, UNRESOLVED_NUM));
        assertRepr(where, "WHERE i = ?");
        assertFalse(where.args().isAllResolved());
    }
}
