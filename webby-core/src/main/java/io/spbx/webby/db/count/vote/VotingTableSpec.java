package io.spbx.webby.db.count.vote;

import io.spbx.orm.api.TableMeta;
import io.spbx.orm.api.query.Column;
import org.jetbrains.annotations.NotNull;

// FIX[minor]: find by annotation?
public record VotingTableSpec(@NotNull TableMeta table,
                              @NotNull Column keyColumn,
                              @NotNull Column actorColumn,
                              @NotNull Column valueColumn) {
}
