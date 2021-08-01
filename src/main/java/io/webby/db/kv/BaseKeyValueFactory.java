package io.webby.db.kv;

import com.google.inject.Inject;
import io.webby.common.Lifetime;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static io.webby.util.EasyCast.castAny;

public abstract class BaseKeyValueFactory implements KeyValueFactory, Closeable {
    protected final Map<String, KeyValueDb<?, ?>> cache = new HashMap<>();

    @Inject
    protected void init(@NotNull Lifetime lifetime) {
        lifetime.onTerminate(this);
    }

    @NotNull
    protected <K, V, KV extends KeyValueDb<K, V>> KV cacheIfAbsent(@NotNull String name, @NotNull Supplier<KV> supplier) {
        return castAny(cache.computeIfAbsent(name, k -> supplier.get()));
    }
}
