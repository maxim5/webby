package io.webby.db.kv.impl;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.Lifetime;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.KeyValueDb;
import io.webby.perf.stats.impl.StatsManager;
import io.webby.util.lazy.LazyBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static io.webby.app.AppConfigException.assure;
import static io.webby.util.base.EasyCast.castAny;
import static io.webby.util.base.EasyObjects.firstNonNull;
import static io.webby.util.base.EasyObjects.firstNonNullIfExist;

public abstract class BaseKeyValueFactory implements InternalKeyValueFactory, Closeable {
    protected final Map<String, KeyValueDb<?, ?>> cache = new HashMap<>();

    @Inject protected Settings settings;
    @Inject protected StatsManager statsManager;
    @Inject protected CodecProvider provider;

    private final LazyBoolean isTrackingKeyValuesOn = new LazyBoolean(() ->
        settings.isProfileMode() && settings.getBoolProperty("perf.track.db.kv.enabled", true)
    );

    @Inject
    protected void init(@NotNull Lifetime lifetime) {
        lifetime.onTerminate(this);
    }

    @Override
    public @NotNull <K, V> KeyValueDb<K, V> getDb(@NotNull DbOptions<K, V> options) {
        KeyValueDb<K, V> internalDb = getInternalDb(options);
        if (isTrackingKeyValuesOn.get()) {
            return new TrackingDbAdapter<>(internalDb, statsManager.newDbListener());
        }
        return internalDb;
    }

    protected <K, V, KV extends KeyValueDb<K, V>> @NotNull KV cacheIfAbsent(@NotNull String name, @NotNull Supplier<KV> supplier) {
        return castAny(cache.computeIfAbsent(name, k -> supplier.get()));
    }

    protected <K, V> @NotNull Codec<K> keyCodecOrDie(@NotNull DbOptions<K, V> options) {
        return firstNonNull(options.keyCodec(), () -> provider.getCodecOrDie(options.key()));
    }

    protected <K, V> @Nullable Codec<K> keyCodecOrNull(@NotNull DbOptions<K, V> options) {
        return firstNonNullIfExist(options.keyCodec(), () -> provider.getCodecOrNull(options.key()));
    }

    protected <K, V> @NotNull Codec<V> valueCodecOrDie(@NotNull DbOptions<K, V> options) {
        return firstNonNull(options.valueCodec(), () -> provider.getCodecOrDie(options.value()));
    }

    protected <K, V> @Nullable Codec<V> valueCodecOrNull(@NotNull DbOptions<K, V> options) {
        return firstNonNullIfExist(options.valueCodec(), () -> provider.getCodecOrNull(options.value()));
    }

    protected static @NotNull String formatFileName(@NotNull String pattern, @NotNull String name) {
        assure(pattern.contains("%s"), "The file pattern must contain '%%s' to use separate file per database: %s", pattern);
        return pattern.formatted(name);
    }

    @Override
    public void close() {
        cache.values().forEach(KeyValueDb::close);
    }
}
