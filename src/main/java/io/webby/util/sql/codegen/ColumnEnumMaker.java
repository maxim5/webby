package io.webby.util.sql.codegen;

import io.webby.util.sql.api.query.TermType;
import io.webby.util.sql.arch.PrefixedColumn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ColumnEnumMaker {
    public static @NotNull Snippet make(@NotNull List<PrefixedColumn> columns) {
        return new Snippet()
            .withLines(columns.stream().map(column -> "%s(TermType.%s)".formatted(column.sqlName(), termTypeOf(column))));
    }

    private static @NotNull TermType termTypeOf(@NotNull PrefixedColumn column) {
        return switch (column.type().jdbcType()) {
            case Boolean -> TermType.BOOL;
            case Int, Long, Short, Byte, Float, Double -> TermType.NUMBER;
            case String, Bytes -> TermType.STRING;
            case Date, Time, Timestamp -> TermType.TIME;
        };
    }
}
