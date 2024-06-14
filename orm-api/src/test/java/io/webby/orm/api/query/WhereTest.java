package io.webby.orm.api.query;

import io.webby.orm.testing.FakeColumn;
import org.junit.jupiter.api.Test;

import static io.webby.orm.api.query.CompareType.GE;
import static io.webby.orm.api.query.CompareType.LE;
import static io.webby.orm.api.query.Shortcuts.*;
import static io.webby.testing.orm.AssertSql.assertThat;

public class WhereTest {
    @Test
    public void compare_expr() {
        assertThat(Where.and(GE.compare(FakeColumn.INT, num(0)))).matches("WHERE i >= 0");
        assertThat(
            Where.and(GE.compare(FakeColumn.INT, num(0)),
                      LE.compare(FakeColumn.INT, num(5)))
        ).matches("WHERE i >= 0 AND i <= 5");
    }

    @Test
    public void like_expr() {
        assertThat(Where.of(like(FakeColumn.STR, FakeColumn.FOO))).matches("WHERE s LIKE foo");
        assertThat(Where.of(like(FakeColumn.STR, literal("%")))).matches("WHERE s LIKE '%'");
        assertThat(Where.of(like(Func.HEX.apply(FakeColumn.STR), literal("foo%")))).matches("WHERE hex(s) LIKE 'foo%'");
    }

    @Test
    public void lookup_variable() {
        Where where = Where.of(lookupBy(FakeColumn.INT, var(1)));
        assertThat(where)
            .matches("WHERE i = ?")
            .containsArgsExactly(1)
            .allArgsResolved();
    }

    @Test
    public void lookup_variables() {
        Where where = Where.of(lookupBy(FakeColumn.INT, UNRESOLVED_NUM));
        assertThat(where).matches("WHERE i = ?").containsUnresolved();
    }
}
