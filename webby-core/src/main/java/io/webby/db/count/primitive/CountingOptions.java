package io.webby.db.count.primitive;

import io.webby.db.StorageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CountingOptions(@NotNull String name,
                              @NotNull StorageType storeType,
                              @Nullable CountingTableSpec tableSpec) {
    public static @NotNull CountingOptions ofKeyValue(@NotNull String name) {
        return new CountingOptions(name, StorageType.KEY_VALUE_DB, null);
    }

    public static @NotNull CountingOptions ofSqlTable(@NotNull String name, @NotNull CountingTableSpec tableSpec) {
        return new CountingOptions(name, StorageType.SQL_DB, tableSpec);
    }
}
