package io.webby.db.count.vote;

import io.webby.orm.api.BaseTable;
import io.webby.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;

// FIX[minor]: find by annotation?
public record VotingTableSpec(@NotNull Class<? extends BaseTable<?>> tableClass,
                              @NotNull Column keyColumn,
                              @NotNull Column actorColumn,
                              @NotNull Column valueColumn) {
}
