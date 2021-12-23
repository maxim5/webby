package io.webby.db.kv.tupl;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import io.webby.util.lazy.AtomicLazy;
import io.webby.util.lazy.DelayedAccessLazy;
import io.webby.util.base.Rethrow;
import org.cojen.tupl.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static io.webby.util.base.Rethrow.rethrow;

public class TuplFactory extends BaseKeyValueFactory {
    private final DelayedAccessLazy<Database> db = AtomicLazy.emptyLazy();

    @Inject private Settings settings;
    @Inject private CodecProvider provider;

    @Override
    public @NotNull <K, V> TuplDb<K, V> getInternalDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            Database database = db.lazyGet(Rethrow.Suppliers.rethrow(() -> Database.open(getDatabaseConfig())));

            try {
                Codec<K> keyCodec = provider.getCodecOrDie(key);
                Codec<V> valueCodec = provider.getCodecOrDie(value);
                Index index = database.openIndex(name);
                return new TuplDb<>(index, database, keyCodec, valueCodec);
            } catch (IOException e) {
                return rethrow(e);
            }
        });
    }

    private @NotNull DatabaseConfig getDatabaseConfig() {
        Path storagePath = settings.storageSettings().keyValueSettingsOrDie().path();
        String filename = settings.getProperty("db.tupl.filename", "tupl.data");
        long cacheMinSize = settings.getLongProperty("db.tupl.cache.min.bytes", 1 << 16);
        long cacheMaxSize = settings.getLongProperty("db.tupl.cache.max.bytes", 10 << 20);
        DurabilityMode durabilityMode = settings.getEnumProperty("db.tupl.durability.mode", DurabilityMode.NO_SYNC);
        LockUpgradeRule lockUpgradeRule = settings.getEnumProperty("db.tupl.lock.upgrade.rule", LockUpgradeRule.STRICT);
        int lockTimeoutMillis = settings.getIntProperty("db.tupl.lock.timeout.millis", 1000);
        long checkpointRateMillis = settings.getIntProperty("db.tupl.checkpoint.rate.millis", 1000);
        long checkpointThresholdBytes = settings.getLongProperty("db.tupl.checkpoint.size.threshold.bytes", 100 << 20);
        int checkpointMaxThreads = settings.getIntProperty("db.tupl.checkpoint.max.threads", 1);
        boolean syncWrites = settings.getBoolProperty("db.tupl.sync.writes.enabled", false);
        boolean readOnly = settings.getBoolProperty("db.tupl.read.only.enabled", false);
        int pageSizeBytes = settings.getIntProperty("db.tupl.page.size.bytes", 4096);
        boolean directPageAccess = settings.getBoolProperty("db.tupl.direct.page.access.enabled", true);
        boolean cachePriming = settings.getBoolProperty("db.tupl.cache.priming", false);
        boolean cleanShutdown = settings.getBoolProperty("db.tupl.clean.shutdown", false);

        assert cacheMinSize <= cacheMaxSize : "Invalid TUPL cache settings: %d vs %d".formatted(cacheMinSize, cacheMaxSize);

        return new DatabaseConfig()
                .baseFile(storagePath.resolve(filename).toFile())
                .minCacheSize(cacheMinSize)
                .maxCacheSize(cacheMaxSize)
                .durabilityMode(durabilityMode)
                .lockUpgradeRule(lockUpgradeRule)
                .lockTimeout(lockTimeoutMillis, TimeUnit.MILLISECONDS)
                .checkpointRate(checkpointRateMillis, TimeUnit.MILLISECONDS)
                .checkpointSizeThreshold(checkpointThresholdBytes)
                .maxCheckpointThreads(checkpointMaxThreads)
                .syncWrites(syncWrites)
                .readOnly(readOnly)
                .pageSize(pageSizeBytes)
                .directPageAccess(directPageAccess)
                .cachePriming(cachePriming)
                .cleanShutdown(cleanShutdown);
    }
}
