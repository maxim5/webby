package io.spbx.webby.db.kv.tupl;

import io.spbx.util.base.Unchecked;
import io.spbx.util.lazy.AtomicCacheCompute;
import io.spbx.util.lazy.CacheCompute;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.kv.DbOptions;
import io.spbx.webby.db.kv.impl.BaseKeyValueFactory;
import org.cojen.tupl.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static io.spbx.util.base.Unchecked.rethrow;

public class TuplFactory extends BaseKeyValueFactory {
    private final CacheCompute<Database> dbCache = AtomicCacheCompute.createEmpty();

    @Override
    public @NotNull <K, V> TuplDb<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options, () -> {
            Database database = openDb();

            try {
                Codec<K> keyCodec = keyCodecOrDie(options);
                Codec<V> valueCodec = valueCodecOrDie(options);
                Index index = database.openIndex(options.name());
                return new TuplDb<>(index, database, keyCodec, valueCodec);
            } catch (IOException e) {
                return rethrow(e);
            }
        });
    }

    private @NotNull Database openDb() {
        return dbCache.getOrCompute(Unchecked.Suppliers.rethrow(() -> Database.open(getDatabaseConfig())));
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
