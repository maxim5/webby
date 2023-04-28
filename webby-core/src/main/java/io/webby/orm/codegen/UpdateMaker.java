package io.webby.orm.codegen;

import io.webby.orm.arch.model.Column;
import io.webby.orm.arch.model.TableArch;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.webby.orm.codegen.Joining.COMMA_JOINER;
import static io.webby.orm.codegen.SqlSupport.EQ_QUESTION;

class UpdateMaker {
    public static @NotNull Snippet make(@NotNull TableArch table, @NotNull List<Column> columns) {
         return new Snippet()
                 .withLine("UPDATE ", table.sqlName())
                 .withLine("SET ", columns.stream().map(Column::sqlName).map(EQ_QUESTION::formatted).collect(COMMA_JOINER));
    }
}
