package io.webby.orm.api.query;

import io.webby.orm.api.Engine;
import io.webby.orm.testing.FakeColumn;
import org.junit.jupiter.api.Test;

import static io.webby.orm.api.query.Shortcuts.num;
import static io.webby.orm.testing.AssertSql.*;

public class SelectWhereTest {
    @Test
    public void select_column_from_table() {
        SelectWhere query = SelectWhere.from("table").select(FakeColumn.INT).build();
        assertRepr(query, """
            SELECT i
            FROM table
            """);
        assertNoArgs(query);
    }

    @Test
    public void select_few_columns_from_table() {
        SelectWhere query = SelectWhere.from("table").select(FakeColumn.INT, FakeColumn.FOO).build();
        assertRepr(query, """
            SELECT i, foo
            FROM table
            """);
        assertNoArgs(query);
    }

    @Test
    public void select_function_from_table() {
        SelectWhere query = SelectWhere.from("table").select(Func.COUNT.apply(FakeColumn.INT)).build();
        assertRepr(query, """
            SELECT count(i)
            FROM table
            """);
        assertNoArgs(query);
    }

    @Test
    public void select_const_from_table() {
        SelectWhere query = SelectWhere.from("table").select(num(77)).build();
        assertRepr(query, """
            SELECT 77
            FROM table
            """);
        assertNoArgs(query);
    }

    @Test
    public void select_column_from_table_where() {
        SelectWhere query = SelectWhere.from("table")
            .select(FakeColumn.INT)
            .where(Where.of(FakeColumn.FOO.bool()))
            .build();
        assertRepr(query, """
            SELECT i
            FROM table
            WHERE foo
            """);
        assertNoArgs(query);
    }

    @Test
    public void select_column_from_table_order_by() {
        SelectWhere query = SelectWhere.from("table")
            .select(FakeColumn.INT)
            .orderBy(OrderBy.of(FakeColumn.STR, Order.ASC))
            .build();
        assertRepr(query, """
            SELECT i
            FROM table
            ORDER BY s ASC
            """);
        assertNoArgs(query);
    }

    @Test
    public void select_column_from_table_offset() {
        SelectWhere query = SelectWhere.from("table")
            .select(FakeColumn.INT)
            .with(Offset.of(5))
            .build();
        assertRepr(query, """
            SELECT i
            FROM table
            OFFSET ?
            """);
        assertArgs(query, 5);
    }

    @Test
    public void select_column_from_table_limit() {
        SelectWhere query = SelectWhere.from("table")
            .select(FakeColumn.INT)
            .with(Limit.of(10))
            .build();
        assertRepr(query, """
            SELECT i
            FROM table
            LIMIT ?
            """);
        assertArgs(query, 10);
    }

    @Test
    public void select_column_from_table_pagination_limit() {
        SelectWhere query = SelectWhere.from("table")
            .select(FakeColumn.INT)
            .with(Pagination.ofOffset(3, 5), Engine.SQLite)
            .build();
        assertRepr(query, """
            SELECT i
            FROM table
            LIMIT ?
            OFFSET ?
            """);
        assertArgs(query, 3, 5);
    }

    @Test
    public void select_column_from_table_pagination_fetch_next() {
        SelectWhere query = SelectWhere.from("table")
            .select(FakeColumn.INT)
            .with(Pagination.ofOffset(3, 5), Engine.Oracle)
            .build();
        assertRepr(query, """
            SELECT i
            FROM table
            FETCH NEXT ? ROWS ONLY
            OFFSET ?
            """);
        assertArgs(query, 3, 5);
    }

    @Test
    public void select_invalid() {
        assertReprThrows(() -> SelectWhere.from("table").build());
        assertReprThrows(() -> SelectWhere.from("table").with(Limit.of(5)).build());
        assertReprThrows(() -> SelectWhere.from("table").select(num(1)).with(Limit.of(5)).with(Limit.of(8)).build());
        assertReprThrows(() -> SelectWhere.from("table").select(num(1)).with(Offset.of(3)).with(Offset.of(9)).build());
    }
}
