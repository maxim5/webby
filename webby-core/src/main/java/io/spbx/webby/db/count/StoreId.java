package io.spbx.webby.db.count;

import io.spbx.webby.db.StorageType;
import org.jetbrains.annotations.NotNull;

public record StoreId(@NotNull StorageType type, @NotNull String name) {
}
