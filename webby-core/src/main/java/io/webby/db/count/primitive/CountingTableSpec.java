package io.webby.db.count.primitive;

import io.webby.orm.api.TableMeta;
import io.webby.orm.api.query.Column;
import io.spbx.util.base.OneOf;
import org.jetbrains.annotations.NotNull;

public record CountingTableSpec(@NotNull TableMeta table,
                                @NotNull Column keyColumn,
                                @NotNull OneOf<Column, Column> valueColumn) {
}
