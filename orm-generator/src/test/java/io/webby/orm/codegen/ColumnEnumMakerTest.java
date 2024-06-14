package io.webby.orm.codegen;

import io.webby.orm.arch.model.Column;
import io.webby.orm.arch.model.JdbcType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.webby.orm.codegen.AssertSnippet.assertThatJava;


public class ColumnEnumMakerTest {
    @Test
    public void make_different_column_types() {
        List<Column> columns = List.of(
            Column.of("a", JdbcType.Boolean),
            Column.of("b", JdbcType.Int),
            Column.of("c", JdbcType.Long),
            Column.of("d", JdbcType.Short),
            Column.of("e", JdbcType.Byte),
            Column.of("f", JdbcType.Float),
            Column.of("g", JdbcType.Double),
            Column.of("h", JdbcType.String),
            Column.of("i", JdbcType.Bytes),
            Column.of("j", JdbcType.Date),
            Column.of("k", JdbcType.Time),
            Column.of("l", JdbcType.Timestamp)
        );
        Snippet snippet = ColumnEnumMaker.make(columns.stream().map(x -> x.prefixed("prefix")).toList());
        assertThatJava(snippet).matches("""
            a(TermType.BOOL)
            b(TermType.NUMBER)
            c(TermType.NUMBER)
            d(TermType.NUMBER)
            e(TermType.NUMBER)
            f(TermType.NUMBER)
            g(TermType.NUMBER)
            h(TermType.STRING)
            i(TermType.STRING)
            j(TermType.TIME)
            k(TermType.TIME)
            l(TermType.TIME)
            """);
    }
}
