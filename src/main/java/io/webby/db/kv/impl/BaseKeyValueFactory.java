package io.webby.db.kv.impl;

import com.google.inject.Inject;
import io.webby.app.AppConfigException;
import io.webby.common.Lifetime;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.KeyValueFactory;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static io.webby.util.EasyCast.castAny;

public abstract class BaseKeyValueFactory implements InternalKeyValueFactory, Closeable {
    protected final Map<String, KeyValueDb<?, ?>> cache = new HashMap<>();

    @Inject
    protected void init(@NotNull Lifetime lifetime) {
        lifetime.onTerminate(this);
    }

    @Override
    public @NotNull <K, V> KeyValueDb<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return getInternalDb(name, key, value);
    }

    protected <K, V, KV extends KeyValueDb<K, V>> @NotNull KV cacheIfAbsent(@NotNull String name, @NotNull Supplier<KV> supplier) {
        return castAny(cache.computeIfAbsent(name, k -> supplier.get()));
    }

    protected static @NotNull String formatFileName(@NotNull String filename, @NotNull String name) {
        AppConfigException.failIf(
                !filename.contains("%s"),
                "The file pattern must contain '%%s' to use separate file per database: %s".formatted(filename)
        );
        return filename.formatted(name);
    }

    @Override
    public void close() throws IOException {
        cache.values().forEach(KeyValueDb::close);
    }
}
