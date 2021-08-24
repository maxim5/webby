package io.webby.db.kv.impl;

import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import org.jetbrains.annotations.NotNull;

public interface InternalKeyValueFactory extends KeyValueFactory {
    <K, V> @NotNull KeyValueDb<K, V> getInternalDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value);
}
