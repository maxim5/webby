package io.webby.util.sql.schema;

import io.webby.util.sql.api.FollowReferences;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WithPrefixedColumns {
    @NotNull List<PrefixedColumn> columns(@NotNull FollowReferences follow);

    default int columnsNumber(@NotNull FollowReferences follow) {
        return columns(follow).size();
    }
}
