package io.webby.util.sql.codegen;

import io.webby.util.sql.arch.Column;
import io.webby.util.sql.arch.TableArch;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.webby.util.sql.codegen.Joining.COMMA_JOINER;
import static io.webby.util.sql.codegen.SqlSupport.EQ_QUESTION;

class UpdateMaker {
    public static @NotNull Snippet make(@NotNull TableArch table, @NotNull List<Column> columns) {
         return new Snippet()
                 .withLine("UPDATE ", table.sqlName())
                 .withLine("SET ", columns.stream().map(Column::sqlName).map(EQ_QUESTION::formatted).collect(COMMA_JOINER));
    }
}
