package io.webby.util.sql.codegen;

import io.webby.util.sql.schema.Column;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static io.webby.util.sql.codegen.ColumnJoins.EQ_QUESTION;

class WhereMaker {
    public static final Collector<CharSequence, ?, String> AND_JOINER = Collectors.joining(" AND ");

    public static @NotNull Snippet makeForAnd(@NotNull List<Column> columns) {
        return new Snippet()
                .withLine("WHERE ", columns.stream().map(Column::sqlName).map(EQ_QUESTION::formatted).collect(AND_JOINER));
    }
}
