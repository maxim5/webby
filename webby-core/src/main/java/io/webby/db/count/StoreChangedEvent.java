package io.webby.db.count;

import org.jetbrains.annotations.NotNull;

public record StoreChangedEvent(@NotNull StoreId storeId) {
}
