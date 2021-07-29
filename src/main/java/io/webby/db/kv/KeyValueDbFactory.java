package io.webby.db.kv;

import org.jetbrains.annotations.NotNull;

public interface KeyValueDbFactory {
    @NotNull
    <K, V> KeyValueDb<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value);
}
