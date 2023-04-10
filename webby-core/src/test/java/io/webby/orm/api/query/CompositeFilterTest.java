package io.webby.orm.api.query;

import io.webby.orm.api.Engine;
import io.webby.orm.testing.FakeColumn;
import org.junit.jupiter.api.Test;

import static io.webby.orm.testing.AssertSql.*;

public class CompositeFilterTest {
    @Test
    public void with_where() {
        CompositeFilter filter = CompositeFilter.builder().with(Where.of(FakeColumn.FOO.bool())).build();
        assertRepr(filter, "WHERE foo");
        assertNoArgs(filter);
    }

    @Test
    public void with_where_two_terms() {
        CompositeFilter filter = CompositeFilter.builder()
            .with(Where.of(FakeColumn.FOO.bool()))
            .with(Where.of(FakeColumn.FOO.bool()))
            .build();
        assertRepr(filter, "WHERE foo AND foo");
        assertNoArgs(filter);
    }

    @Test
    public void with_order_by() {
        CompositeFilter filter = CompositeFilter.builder().with(OrderBy.of(FakeColumn.INT, Order.ASC)).build();
        assertRepr(filter, "ORDER BY i ASC");
        assertNoArgs(filter);
    }

    @Test
    public void with_order_by_two_terms() {
        CompositeFilter filter = CompositeFilter.builder()
            .with(OrderBy.of(FakeColumn.INT, Order.ASC))
            .with(OrderBy.of(FakeColumn.FOO, Order.DESC))
            .build();
        assertRepr(filter, "ORDER BY i ASC, foo DESC");
        assertNoArgs(filter);
    }

    @Test
    public void with_limit() {
        CompositeFilter filter = CompositeFilter.builder().with(Limit.of(55)).build();
        assertRepr(filter, "LIMIT ?");
        assertArgs(filter, 55);
    }

    @Test
    public void with_offset() {
        CompositeFilter filter = CompositeFilter.builder().with(Offset.of(88)).build();
        assertRepr(filter, "OFFSET ?");
        assertArgs(filter, 88);
    }

    @Test
    public void with_pagination_offset_sqlite() {
        CompositeFilter filter = CompositeFilter.builder().with(Pagination.ofOffset(3, 5), Engine.SQLite).build();
        assertRepr(filter, """
            LIMIT ?
            OFFSET ?
            """);
        assertArgs(filter, 3, 5);
    }

    @Test
    public void with_pagination_offset_oracle() {
        CompositeFilter filter = CompositeFilter.builder().with(Pagination.ofOffset(3, 5), Engine.Oracle).build();
        assertRepr(filter, """
            FETCH NEXT ? ROWS ONLY
            OFFSET ?
            """);
        assertArgs(filter, 3, 5);
    }

    @Test
    public void with_pagination_of_column_sqlite() {
        CompositeFilter filter = CompositeFilter.builder()
            .with(Pagination.ofColumn(FakeColumn.FOO.makeVar(99), Order.ASC, 10), Engine.SQLite)
            .build();
        assertRepr(filter, """
            WHERE foo > ?
            LIMIT ?
            """);
        assertArgs(filter, 99, 10);
    }

    @Test
    public void with_pagination_first_page_sqlite() {
        CompositeFilter filter = CompositeFilter.builder()
            .with(Pagination.firstPage(777), Engine.SQLite)
            .build();
        assertRepr(filter, "LIMIT ?");
        assertArgs(filter, 777);
    }
}
