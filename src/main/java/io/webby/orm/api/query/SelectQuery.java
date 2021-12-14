package io.webby.orm.api.query;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;

public interface SelectQuery extends Representable, ArgsHolder {
    @NotNull SelectQuery withTable(@NotNull String tableName);

    default @NotNull SelectQuery withTable(@NotNull TableMeta meta) {
        return withTable(meta.sqlTableName());
    }

    default @NotNull SelectQuery withTable(@NotNull BaseTable<?> table) {
        return withTable(table.meta());
    }
}
