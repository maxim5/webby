package io.webby.db.count.vote;

import io.webby.orm.api.TableMeta;
import io.webby.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;

// FIX[minor]: find by annotation?
public record VotingTableSpec(@NotNull TableMeta table,
                              @NotNull Column keyColumn,
                              @NotNull Column actorColumn,
                              @NotNull Column valueColumn) {
}
