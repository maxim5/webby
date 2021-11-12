package io.webby.util.sql.codegen;

import io.webby.util.sql.schema.Column;
import io.webby.util.sql.schema.TableSchema;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.webby.util.sql.codegen.ColumnJoins.*;

class UpdateMaker {
    public static @NotNull Snippet make(@NotNull TableSchema table, @NotNull List<Column> columns) {
         return new Snippet()
                 .withLine("UPDATE ", table.sqlName())
                 .withLine("SET ", columns.stream().map(Column::sqlName).map(EQ_QUESTION::formatted).collect(COMMA_JOINER));
    }
}
