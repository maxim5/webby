package io.webby.util.sql.codegen;

import io.webby.util.sql.api.ReadFollow;
import io.webby.util.sql.arch.PrefixedColumn;
import io.webby.util.sql.arch.TableField;
import io.webby.util.sql.arch.TableArch;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static io.webby.util.sql.codegen.SqlSupport.EQ_QUESTION;

class WhereMaker {
    public static final Collector<CharSequence, ?, String> AND_JOINER = Collectors.joining(" AND ");

    public static @NotNull Snippet makeForPrimaryColumns(@NotNull TableArch table) {
        return make(table.columns(ReadFollow.NO_FOLLOW, TableField::isPrimaryKey), AND_JOINER);
    }

    public static @NotNull Snippet make(@NotNull List<PrefixedColumn> columns,
                                        @NotNull Collector<CharSequence, ?, String> collector) {
        return new Snippet()
            .withLine("WHERE ", columns.stream().map(PrefixedColumn::sqlPrefixedName).map(EQ_QUESTION::formatted).collect(collector));
    }
}
