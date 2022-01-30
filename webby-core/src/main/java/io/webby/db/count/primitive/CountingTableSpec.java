package io.webby.db.count.primitive;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.query.Column;
import io.webby.util.collect.OneOf;
import org.jetbrains.annotations.NotNull;

public record CountingTableSpec(@NotNull Class<? extends BaseTable<?>> tableClass,
                                @NotNull Column keyColumn,
                                @NotNull OneOf<Column, Column> valueColumn) {
}
