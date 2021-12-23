package io.webby.db.kv.halodb;

import com.google.inject.Inject;
import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBOptions;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

import static io.webby.util.base.Rethrow.Suppliers.runRethrow;

public class HaloDbFactory extends BaseKeyValueFactory {
    @Inject private Settings settings;
    @Inject private CodecProvider provider;

    @Override
    public @NotNull <K, V> HaloDbImpl<K, V> getInternalDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            Codec<K> keyCodec = provider.getCodecOrDie(key);
            Codec<V> valueCodec = provider.getCodecOrDie(value);

            Path storagePath = settings.storageSettings().keyValueSettingsOrDie().path();
            HaloDBOptions options = getOptions();
            if (keyCodec.size().isFixed()) {
                options.setFixedKeySize(keyCodec.size().numBytes());
            }
            HaloDB db = runRethrow(() -> HaloDB.open(storagePath.resolve(name).toFile(), options));
            return new HaloDbImpl<>(db, keyCodec, valueCodec);
        });
    }

    private @NotNull HaloDBOptions getOptions() {
        int maxFileSize = settings.getIntProperty("db.halodb.data.max.file.size.bytes", 1 << 20);
        int maxTombstoneFileSize = settings.getIntProperty("db.halodb.tombstone.max.file.size.bytes", 64 << 20);
        int buildIndexThreads = settings.getIntProperty("db.halodb.build.index.threads", 1);
        int flushDataSizeBytes = settings.getIntProperty("db.halodb.data.flush.threshold.bytes", 10 << 20);
        double compactionThresholdPerFile = settings.getDoubleProperty("db.halodb.data.compaction.threshold", 0.75);
        int compactionJobRate = settings.getIntProperty("db.halodb.compaction.job.rate.bytes", 1 << 20);
        int numberOfRecords = settings.getIntProperty("db.halodb.records.number", 1_000_000);
        boolean cleanUpTombstones = settings.getBoolProperty("db.halodb.tombstone.cleanup.during.open.enabled", true);
        boolean cleanUpInMemoryIndex = settings.getBoolProperty("db.halodb.memory.cleanup.on.close.enabled", false);
        boolean useMemoryPool = settings.getBoolProperty("db.halodb.memory.pool.enabled", false);
        int memoryPoolChunkSize = settings.getIntProperty("db.halodb.memory.pool.chunk.size.bytes", 16 << 20);
        int fixedKeySize = settings.getIntProperty("db.halodb.memory.pool.fixed.key.size", 127);

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
