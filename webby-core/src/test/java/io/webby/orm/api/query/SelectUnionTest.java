package io.webby.orm.api.query;

import io.webby.orm.testing.AssertSql;
import io.webby.orm.testing.FakeColumn;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static io.webby.orm.api.query.Func.COUNT;
import static io.webby.orm.api.query.Func.MAX;
import static io.webby.orm.api.query.Shortcuts.STAR;
import static io.webby.orm.api.query.Shortcuts.num;

public class SelectUnionTest {
    @Test
    public void select_where_union() {
        SelectWhere query1 = SelectWhere.from("foo").select(COUNT.apply(FakeColumn.INT)).build();
        SelectWhere query2 = SelectWhere.from("bar").select(num(77)).build();
        assertThat(query1.union(query2))
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT count(i)
                FROM foo
                UNION
                SELECT 77
                FROM bar
                """)
            .containsNoArgs()
            .isEqualTo(query1.toBuilder().union(query2).build());
    }

    @Test
    public void select_group_by_union() {
        SelectGroupBy query1 = SelectGroupBy.from("foo").select(FakeColumn.INT, COUNT.apply(STAR)).build();
        SelectGroupBy query2 = SelectGroupBy.from("foo").select(FakeColumn.FOO, MAX.apply(STAR)).build();
        assertThat(query1.union(query2))
            .hasConsistentIntern()
            .hasConsistentBuilder()
            .matches("""
                SELECT i, count(*)
                FROM foo
                GROUP BY i
                UNION
                SELECT foo, max(*)
                FROM foo
                GROUP BY foo
                """)
            .containsNoArgs();
    }

    private static @NotNull SelectUnionSubject assertThat(@NotNull SelectUnion query) {
        return new SelectUnionSubject(query);
    }

    private static class SelectUnionSubject extends AssertSql.UnitSubject<SelectUnionSubject> {
        private final SelectUnion query;

        private SelectUnionSubject(@NotNull SelectUnion query) {
            super(query);
            this.query = query;
        }

        public @NotNull SelectUnionSubject hasConsistentIntern() {
            AssertSql.assertThat((Unit) query.intern()).isEqualTo(query);
            return this;
        }

        public @NotNull SelectUnionSubject hasConsistentBuilder() {
            AssertSql.assertThat(query.toBuilder().build()).isEqualTo(query);
            return this;
        }
    }
}
