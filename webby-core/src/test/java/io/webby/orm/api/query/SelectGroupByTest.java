package io.webby.orm.api.query;

import io.webby.orm.testing.AssertSql;
import io.webby.orm.testing.AssertSql.UnitSubject;
import io.webby.orm.testing.FakeColumn;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static io.webby.orm.api.query.Func.COUNT;
import static io.webby.orm.api.query.Func.MAX;
import static io.webby.orm.api.query.Shortcuts.STAR;
import static io.webby.orm.testing.PersonTableData.PERSON_META;

public class SelectGroupByTest {
    @Test
    public void select_one_column_group_by_one_from_table() {
        SelectGroupBy query = SelectGroupBy.from("table").select(FakeColumn.INT, COUNT.apply(STAR)).build();
        assertThat(query)
            .hasConsistentIntern()
            .matches("""
                SELECT i, count(*)
                FROM table
                GROUP BY i
                """)
            .containsNoArgs();
    }

    @Test
    public void select_one_column_group_by_two_from_table() {
        SelectGroupBy query = SelectGroupBy.from(PERSON_META)
            .groupBy(FakeColumn.FOO, FakeColumn.STR)
            .aggregate(MAX.apply(FakeColumn.INT))
            .build();
        assertThat(query)
            .hasConsistentIntern()
            .matches("""
                SELECT foo, s, max(i)
                FROM person
                GROUP BY foo, s
                """)
            .containsNoArgs();
    }

    private static @NotNull SelectGroupBySubject assertThat(@NotNull SelectGroupBy query) {
        return new SelectGroupBySubject(query);
    }

    private static class SelectGroupBySubject extends UnitSubject<SelectGroupBySubject> {
        private final SelectGroupBy query;

        private SelectGroupBySubject(@NotNull SelectGroupBy query) {
            super(query);
            this.query = query;
        }

        public @NotNull SelectGroupBySubject hasConsistentIntern() {
            AssertSql.assertThat((Unit) query.intern()).isEqualTo(query);
            return this;
        }
    }
}
