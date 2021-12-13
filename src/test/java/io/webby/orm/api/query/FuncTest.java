package io.webby.orm.api.query;

import org.junit.jupiter.api.Test;

import static io.webby.orm.api.query.AssertSql.assertRepr;
import static io.webby.orm.api.query.AssertSql.assertReprThrows;
import static io.webby.orm.api.query.Shortcuts.*;
import static io.webby.orm.api.query.TermType.*;

public class FuncTest {
    @Test
    public void aggregations() {
        assertRepr(Func.COUNT.apply(FakeColumn.FOO), "count(foo)", NUMBER);
        assertRepr(Func.COUNT.apply(FakeColumn.INT), "count(i)", NUMBER);
        assertRepr(Func.COUNT.apply(FakeColumn.STR), "count(s)", NUMBER);

        assertRepr(Func.SUM.apply(FakeColumn.FOO), "sum(foo)", NUMBER);
        assertRepr(Func.SUM.apply(FakeColumn.INT), "sum(i)", NUMBER);
        assertReprThrows(() -> Func.SUM.apply(FakeColumn.STR));

        assertRepr(Func.AVG.apply(FakeColumn.FOO), "avg(foo)", NUMBER);
        assertRepr(Func.AVG.apply(FakeColumn.INT), "avg(i)", NUMBER);
        assertReprThrows(() -> Func.AVG.apply(FakeColumn.STR));

        assertRepr(Func.MIN.apply(FakeColumn.FOO), "min(foo)", NUMBER);
        assertRepr(Func.MIN.apply(FakeColumn.INT), "min(i)", NUMBER);
        assertReprThrows(() -> Func.MIN.apply(FakeColumn.STR));

        assertRepr(Func.MAX.apply(FakeColumn.FOO), "max(foo)", NUMBER);
        assertRepr(Func.MAX.apply(FakeColumn.INT), "max(i)", NUMBER);
        assertReprThrows(() -> Func.MAX.apply(FakeColumn.STR));

        assertRepr(Func.FIRST.apply(FakeColumn.FOO), "first(foo)", WILDCARD);
        assertRepr(Func.FIRST_NUM.apply(FakeColumn.INT), "first(i)", NUMBER);
        assertRepr(Func.FIRST_STR.apply(FakeColumn.STR), "first(s)", STRING);

        assertRepr(Func.LAST.apply(FakeColumn.FOO), "last(foo)", WILDCARD);
        assertRepr(Func.LAST_NUM.apply(FakeColumn.INT), "last(i)", NUMBER);
        assertRepr(Func.LAST_STR.apply(FakeColumn.STR), "last(s)", STRING);
    }

    @Test
    public void strings_case() {
        assertRepr(Func.LOWER.apply(literal("ABC")), "lower('ABC')", STRING);
        assertReprThrows(() -> Func.LOWER.apply(FakeColumn.INT));

        assertRepr(Func.LCASE.apply(literal("ABC")), "lcase('ABC')", STRING);
        assertReprThrows(() -> Func.LCASE.apply(FakeColumn.INT));

        assertRepr(Func.UPPER.apply(literal("ABC")), "upper('ABC')", STRING);
        assertReprThrows(() -> Func.UPPER.apply(FakeColumn.INT));

        assertRepr(Func.UCASE.apply(literal("ABC")), "ucase('ABC')", STRING);
        assertReprThrows(() -> Func.UCASE.apply(FakeColumn.INT));
    }

    @Test
    public void strings_substring() {
        assertRepr(Func.SUBSTRING.apply(FakeColumn.STR, num(1), num(2)), "substring(s, 1, 2)", STRING);
        assertRepr(Func.SUBSTRING.apply(literal("ABC"), num(1), num(3)), "substring('ABC', 1, 3)", STRING);
        assertReprThrows(() -> Func.SUBSTRING.apply(num(0), num(1), num(3)));
        assertReprThrows(() -> Func.SUBSTRING.apply(FakeColumn.STR, FakeColumn.STR, FakeColumn.STR));
        assertReprThrows(() -> Func.SUBSTRING.apply(literal("ABC")));
        assertReprThrows(() -> Func.SUBSTRING.apply(literal("ABC"), num(0)));
    }

    @Test
    public void strings_translate() {
        assertRepr(Func.TRANSLATE.apply(FakeColumn.STR, literal("x"), literal("y")), "translate(s, 'x', 'y')", STRING);
        assertRepr(Func.TRANSLATE.apply(literal("X"), FakeColumn.STR, FakeColumn.FOO), "translate('X', s, foo)", STRING);
        assertReprThrows(() -> Func.TRANSLATE.apply(literal("ABC"), num(1), num(3)));
        assertReprThrows(() -> Func.TRANSLATE.apply(num(0), num(1), num(3)));
        assertReprThrows(() -> Func.TRANSLATE.apply(literal("ABC")));
        assertReprThrows(() -> Func.TRANSLATE.apply(literal("ABC"), num(0)));
        assertReprThrows(() -> Func.TRANSLATE.apply(literal("ABC"), num(0), num(1)));
    }

    @Test
    public void coalesce() {
        assertRepr(Func.COALESCE.apply(NULL, num(1)), "coalesce(NULL, 1)", WILDCARD);
        assertRepr(Func.COALESCE3.apply(NULL, num(1), literal("foo")), "coalesce(NULL, 1, 'foo')", WILDCARD);
        assertRepr(Func.COALESCE4.apply(NULL, NULL, num(1), FakeColumn.FOO), "coalesce(NULL, NULL, 1, foo)", WILDCARD);
        assertRepr(Func.COALESCE5.apply(NULL, NULL, num(1), NULL, FakeColumn.FOO), "coalesce(NULL, NULL, 1, NULL, foo)", WILDCARD);
    }
}
