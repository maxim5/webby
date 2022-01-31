package io.webby.db.count;

import io.webby.db.StorageType;
import org.jetbrains.annotations.NotNull;

public record StoreId(@NotNull StorageType type, @NotNull String name) {
}
