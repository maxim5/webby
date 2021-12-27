package io.webby.db.kv;

import org.jetbrains.annotations.NotNull;

public interface KeyValueFactory {
    <K, V> @NotNull KeyValueDb<K, V> getDb(@NotNull DbOptions<K, V> options);
}
