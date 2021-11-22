package io.webby.orm.codegen;

import io.webby.orm.arch.Column;
import io.webby.orm.arch.TableArch;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

import static io.webby.orm.codegen.Joining.COMMA_JOINER;

class InsertMaker {
    public static @NotNull Snippet makeAll(@NotNull TableArch table) {
        return make(table, table.columns());
    }

    public static @NotNull Snippet make(@NotNull TableArch table, @NotNull List<Column> columns) {
         return new Snippet()
                 .withLine("INSERT INTO ", table.sqlName(), " (", columns.stream().map(Column::sqlName).collect(COMMA_JOINER), ")")
                 .withLine("VALUES (", Stream.generate(() -> "?").limit(columns.size()).collect(COMMA_JOINER), ")");
    }
}
