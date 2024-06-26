package io.spbx.webby.db.count.primitive;

import io.spbx.webby.db.StorageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CountingOptions(@NotNull String name,
                              @NotNull StorageType storeType,
                              @Nullable CountingTableSpec tableSpec) {
    public static @NotNull CountingOptions ofKeyValue(@NotNull String name) {
        return new CountingOptions(name, StorageType.KEY_VALUE_DB, null);
    }

    public static @NotNull CountingOptions ofSqlTable(@NotNull CountingTableSpec tableSpec) {
        return new CountingOptions(tableSpec.table().sqlTableName(), StorageType.SQL_DB, tableSpec);
    }
}
