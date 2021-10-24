package io.webby.util.sql.schema;

import com.google.common.collect.Streams;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ColumnJoins {
    public static final String EQ_QUESTION = "%s=?";
    public static final Collector<CharSequence, ?, String> COMMA_JOINER = Collectors.joining(", ");

    public static @NotNull String joinWithComma(@NotNull Iterable<Column> columns) {
        return Streams.stream(columns).map(Column::sqlName).collect(COMMA_JOINER);
    }

    public static @NotNull String joinWithPattern(@NotNull Iterable<Column> columns, @NotNull String pattern) {
        return Streams.stream(columns).map(Column::sqlName).map(pattern::formatted).collect(COMMA_JOINER);
    }
}
