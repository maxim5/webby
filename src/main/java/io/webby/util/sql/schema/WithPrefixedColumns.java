package io.webby.util.sql.schema;

import io.webby.util.sql.api.ReadFollow;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WithPrefixedColumns {
    @NotNull List<PrefixedColumn> columns(@NotNull ReadFollow follow);

    default int columnsNumber(@NotNull ReadFollow follow) {
        return columns(follow).size();
    }
}
