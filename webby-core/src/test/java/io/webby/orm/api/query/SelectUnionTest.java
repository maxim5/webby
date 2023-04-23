package io.webby.orm.api.query;

import io.webby.orm.testing.FakeColumn;
import org.junit.jupiter.api.Test;

import static io.webby.orm.api.query.Shortcuts.num;
import static io.webby.orm.testing.AssertSql.assertThat;

public class SelectUnionTest {
    @Test
    public void select_where_union() {
        SelectWhere query1 = SelectWhere.from("foo").select(Func.COUNT.apply(FakeColumn.INT)).build();
        SelectWhere query2 = SelectWhere.from("bar").select(num(77)).build();
        assertThat(query1.union(query2))
            .matches("""
                SELECT count(i)
                FROM foo
                UNION
                SELECT 77
                FROM bar
                """)
            .containsNoArgs();
    }
}
