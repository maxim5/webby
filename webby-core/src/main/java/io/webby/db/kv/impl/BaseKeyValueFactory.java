package io.webby.db.kv.impl;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.Lifetime;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.KeyValueDb;
import io.webby.perf.stats.impl.StatsManager;
import io.spbx.util.lazy.LazyBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static io.webby.app.AppConfigException.assure;
import static io.spbx.util.base.EasyCast.castAny;
import static io.spbx.util.base.EasyNulls.firstNonNull;
import static io.spbx.util.base.EasyNulls.firstNonNullIfExist;

public abstract class BaseKeyValueFactory implements InternalKeyValueFactory {
    protected final Map<String, KeyValueDb<?, ?>> cache = new HashMap<>();

    @Inject protected Settings settings;
    @Inject protected StatsManager statsManager;
    @Inject protected CodecProvider provider;
    @Inject protected Lifetime lifetime;

    private final LazyBoolean isTrackingKeyValuesOn = new LazyBoolean(() ->
        settings.isProfileMode() && settings.getBoolProperty("perf.track.db.kv.enabled", true)
    );

    @Override
    public @NotNull <K, V> KeyValueDb<K, V> getDb(@NotNull DbOptions<K, V> options) {
        KeyValueDb<K, V> internalDb = getInternalDb(options);
        if (isTrackingKeyValuesOn.get()) {
            return new TrackingDbAdapter<>(internalDb, statsManager.newDbListener());
        }
        return internalDb;
    }

    protected <K, V, KV extends KeyValueDb<K, V>> @NotNull KV cacheIfAbsent(@NotNull DbOptions<K, V> options,
                                                                            @NotNull Supplier<KV> supplier) {
        KeyValueDb<?, ?> db = cache.computeIfAbsent(options.name(), k -> supplier.get());
        lifetime.onTerminate(db);
        return castAny(db);
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
}
