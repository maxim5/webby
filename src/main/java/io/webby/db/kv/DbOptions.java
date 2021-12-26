package io.webby.db.kv;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record DbOptions<K, V>(@NotNull String name,
                              @NotNull Class<K> key,
                              @NotNull Class<V> value,
                              @Nullable StorageType storageType) {
    public static <K, V> @NotNull DbOptions<K, V> of(@NotNull String name,
                                                     @NotNull Class<K> key,
                                                     @NotNull Class<V> value) {
        return new DbOptions<>(name, key, value, null);
    }
}
