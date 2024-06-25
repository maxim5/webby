package io.spbx.webby.db.kv.halodb;

import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBOptions;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.kv.DbOptions;
import io.spbx.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

import static io.spbx.util.base.Unchecked.Suppliers.runRethrow;

public class HaloDbFactory extends BaseKeyValueFactory {
    @Override
    public @NotNull <K, V> HaloDbImpl<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options, () -> {
            Codec<K> keyCodec = keyCodecOrDie(options);
            Codec<V> valueCodec = valueCodecOrDie(options);

            Path storagePath = settings.storageSettings().keyValueSettingsOrDie().path();
            HaloDBOptions haloOptions = getHaloOptions();
            if (keyCodec.size().isFixed()) {
                haloOptions.setFixedKeySize(keyCodec.size().numBytes());
            }
            HaloDB db = runRethrow(() -> HaloDB.open(storagePath.resolve(options.name()).toFile(), haloOptions));
            return new HaloDbImpl<>(db, keyCodec, valueCodec);
        });
    }

    private @NotNull HaloDBOptions getHaloOptions() {
        int maxFileSize = settings.getInt("db.halodb.data.max.file.size.bytes", 1 << 20);
        int maxTombstoneFileSize = settings.getInt("db.halodb.tombstone.max.file.size.bytes", 64 << 20);
        int buildIndexThreads = settings.getInt("db.halodb.build.index.threads", 1);
        int flushDataSizeBytes = settings.getInt("db.halodb.data.flush.threshold.bytes", 10 << 20);
        double compactionThresholdPerFile = settings.getDouble("db.halodb.data.compaction.threshold", 0.75);
        int compactionJobRate = settings.getInt("db.halodb.compaction.job.rate.bytes", 1 << 20);
        int numberOfRecords = settings.getInt("db.halodb.records.number", 1_000_000);
        boolean cleanUpTombstones = settings.getBool("db.halodb.tombstone.cleanup.during.open.enabled", true);
        boolean cleanUpInMemoryIndex = settings.getBool("db.halodb.memory.cleanup.on.close.enabled", false);
        boolean useMemoryPool = settings.getBool("db.halodb.memory.pool.enabled", false);
        int memoryPoolChunkSize = settings.getInt("db.halodb.memory.pool.chunk.size.bytes", 16 << 20);
        int fixedKeySize = settings.getInt("db.halodb.memory.pool.fixed.key.size", 127);

        HaloDBOptions options = new HaloDBOptions();
        options.setMaxFileSize(maxFileSize);
        options.setMaxTombstoneFileSize(maxTombstoneFileSize);
        options.setBuildIndexThreads(buildIndexThreads);
        options.setFlushDataSizeBytes(flushDataSizeBytes);
        options.setCompactionThresholdPerFile(compactionThresholdPerFile);
        options.setCompactionJobRate(compactionJobRate);
        options.setNumberOfRecords(numberOfRecords);
        options.setCleanUpTombstonesDuringOpen(cleanUpTombstones);
        options.setCleanUpInMemoryIndexOnClose(cleanUpInMemoryIndex);
        options.setUseMemoryPool(useMemoryPool);
        options.setMemoryPoolChunkSize(memoryPoolChunkSize);
        options.setFixedKeySize(fixedKeySize);
        return options;
    }
}
