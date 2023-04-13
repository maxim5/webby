package io.webby.orm.api.query;

import io.webby.orm.testing.FakeColumn;
import io.webby.orm.testing.PersonTableData.PersonColumn;
import org.junit.jupiter.api.Test;

import static io.webby.orm.api.query.Shortcuts.*;
import static io.webby.orm.api.query.TermType.*;
import static io.webby.orm.testing.AssertSql.assertReprThrows;
import static io.webby.orm.testing.AssertSql.assertTerm;

public class FuncTest {
    @Test
    public void aggregations() {
        assertTerm(Func.COUNT.apply(FakeColumn.FOO)).matches("count(foo)").hasType(NUMBER);
        assertTerm(Func.COUNT.apply(FakeColumn.INT)).matches("count(i)").hasType(NUMBER);
        assertTerm(Func.COUNT.apply(FakeColumn.STR)).matches("count(s)").hasType(NUMBER);

        assertTerm(Func.SUM.apply(FakeColumn.FOO)).matches("sum(foo)").hasType(NUMBER);
        assertTerm(Func.SUM.apply(FakeColumn.INT)).matches("sum(i)").hasType(NUMBER);
        assertReprThrows(() -> Func.SUM.apply(FakeColumn.STR));

        assertTerm(Func.AVG.apply(FakeColumn.FOO)).matches("avg(foo)").hasType(NUMBER);
        assertTerm(Func.AVG.apply(FakeColumn.INT)).matches("avg(i)").hasType(NUMBER);
        assertReprThrows(() -> Func.AVG.apply(FakeColumn.STR));

        assertTerm(Func.MIN.apply(FakeColumn.FOO)).matches("min(foo)").hasType(NUMBER);
        assertTerm(Func.MIN.apply(FakeColumn.INT)).matches("min(i)").hasType(NUMBER);
        assertReprThrows(() -> Func.MIN.apply(FakeColumn.STR));

        assertTerm(Func.MAX.apply(FakeColumn.FOO)).matches("max(foo)").hasType(NUMBER);
        assertTerm(Func.MAX.apply(FakeColumn.INT)).matches("max(i)").hasType(NUMBER);
        assertReprThrows(() -> Func.MAX.apply(FakeColumn.STR));

        assertTerm(Func.FIRST.apply(FakeColumn.FOO)).matches("first(foo)").hasType(WILDCARD);
        assertTerm(Func.FIRST_NUM.apply(FakeColumn.INT)).matches("first(i)").hasType(NUMBER);
        assertTerm(Func.FIRST_STR.apply(FakeColumn.STR)).matches("first(s)").hasType(STRING);

        assertTerm(Func.LAST.apply(FakeColumn.FOO)).matches("last(foo)").hasType(WILDCARD);
        assertTerm(Func.LAST_NUM.apply(FakeColumn.INT)).matches("last(i)").hasType(NUMBER);
        assertTerm(Func.LAST_STR.apply(FakeColumn.STR)).matches("last(s)").hasType(STRING);
    }

    @Test
    public void strings_case() {
        assertTerm(Func.LOWER.apply(literal("ABC"))).matches("lower('ABC')").hasType(STRING);
        assertReprThrows(() -> Func.LOWER.apply(FakeColumn.INT));

        assertTerm(Func.LCASE.apply(literal("ABC"))).matches("lcase('ABC')").hasType(STRING);
        assertReprThrows(() -> Func.LCASE.apply(FakeColumn.INT));

        assertTerm(Func.UPPER.apply(literal("ABC"))).matches("upper('ABC')").hasType(STRING);
        assertReprThrows(() -> Func.UPPER.apply(FakeColumn.INT));

        assertTerm(Func.UCASE.apply(literal("ABC"))).matches("ucase('ABC')").hasType(STRING);
        assertReprThrows(() -> Func.UCASE.apply(FakeColumn.INT));
    }

    @Test
    public void strings_substring() {
        assertTerm(Func.SUBSTRING.apply(FakeColumn.STR, num(1), num(2)))
            .matches("substring(s, 1, 2)")
            .hasType(STRING);
        assertTerm(Func.SUBSTRING.apply(literal("ABC"), num(1), num(3)))
            .matches("substring('ABC', 1, 3)")
            .hasType(STRING);

        assertReprThrows(() -> Func.SUBSTRING.apply(num(0), num(1), num(3)));
        assertReprThrows(() -> Func.SUBSTRING.apply(FakeColumn.STR, FakeColumn.STR, FakeColumn.STR));
        assertReprThrows(() -> Func.SUBSTRING.apply(literal("ABC")));
        assertReprThrows(() -> Func.SUBSTRING.apply(literal("ABC"), num(0)));
    }

    @Test
    public void strings_translate() {
        assertTerm(Func.TRANSLATE.apply(FakeColumn.STR, literal("x"), literal("y")))
            .matches("translate(s, 'x', 'y')")
            .hasType(STRING);
        assertTerm(Func.TRANSLATE.apply(literal("X"), FakeColumn.STR, FakeColumn.FOO))
            .matches("translate('X', s, foo)")
            .hasType(STRING);

        assertReprThrows(() -> Func.TRANSLATE.apply(literal("ABC"), num(1), num(3)));
        assertReprThrows(() -> Func.TRANSLATE.apply(num(0), num(1), num(3)));
        assertReprThrows(() -> Func.TRANSLATE.apply(literal("ABC")));
        assertReprThrows(() -> Func.TRANSLATE.apply(literal("ABC"), num(0)));
        assertReprThrows(() -> Func.TRANSLATE.apply(literal("ABC"), num(0), num(1)));
    }

    @Test
    public void string_length() {
        assertTerm(Func.LENGTH.apply(FakeColumn.STR)).matches("length(s)").hasType(NUMBER);
        assertReprThrows(() -> Func.LENGTH.apply(PersonColumn.birthday));
    }

    @Test
    public void cast_as() {
        assertTerm(Func.CAST_AS.apply(NULL, new HardcodedStringTerm("TEXT"))).matches("CAST(NULL AS TEXT)").hasType(WILDCARD);
        assertTerm(Func.CAST_AS_SIGNED.apply(FakeColumn.FOO)).matches("CAST(foo AS SIGNED)").hasType(NUMBER);
        assertTerm(Func.CAST_AS_SIGNED.apply(FakeColumn.INT)).matches("CAST(i AS SIGNED)").hasType(NUMBER);
        assertTerm(Func.CAST_AS_SIGNED.apply(FakeColumn.STR)).matches("CAST(s AS SIGNED)").hasType(NUMBER);
        assertTerm(Func.CAST_AS_CHAR.apply(FakeColumn.FOO)).matches("CAST(foo AS CHAR)").hasType(STRING);
        assertTerm(Func.CAST_AS_CHAR.apply(FakeColumn.INT)).matches("CAST(i AS CHAR)").hasType(STRING);
        assertTerm(Func.CAST_AS_CHAR.apply(FakeColumn.STR)).matches("CAST(s AS CHAR)").hasType(STRING);
    }

    @Test
    public void coalesce() {
        assertTerm(Func.COALESCE.apply(NULL, num(1)))
            .matches("coalesce(NULL, 1)")
            .hasType(WILDCARD);
        assertTerm(Func.COALESCE3.apply(NULL, num(1), literal("foo")))
            .matches("coalesce(NULL, 1, 'foo')")
            .hasType(WILDCARD);
        assertTerm(Func.COALESCE4.apply(NULL, NULL, num(1), FakeColumn.FOO))
            .matches("coalesce(NULL, NULL, 1, foo)")
            .hasType(WILDCARD);
        assertTerm(Func.COALESCE5.apply(NULL, NULL, num(1), NULL, FakeColumn.FOO))
            .matches("coalesce(NULL, NULL, 1, NULL, foo)")
            .hasType(WILDCARD);
    }
}
