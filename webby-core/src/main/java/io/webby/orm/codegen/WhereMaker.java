package io.webby.orm.codegen;

import io.webby.orm.api.ReadFollow;
import io.webby.orm.api.query.BoolOpType;
import io.webby.orm.arch.model.PrefixedColumn;
import io.webby.orm.arch.model.TableArch;
import io.webby.orm.arch.model.TableField;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collector;

import static io.webby.orm.codegen.SqlSupport.EQ_QUESTION;

class WhereMaker {
    public static @NotNull Snippet makeForPrimaryColumns(@NotNull TableArch table) {
        assert table.hasPrimaryKeyField() : "No primary columns to use: " + table.sqlName();
        return make(table.columns(ReadFollow.NO_FOLLOW, TableField::isPrimaryKey), BoolOpType.AND.joiner());
    }

    public static @NotNull Snippet make(@NotNull List<PrefixedColumn> columns,
                                        @NotNull Collector<CharSequence, ?, String> collector) {
        return new Snippet()
            .withLine("WHERE ", columns.stream().map(PrefixedColumn::sqlPrefixedName).map(EQ_QUESTION::formatted).collect(collector));
    }
}
