package io.webby.db.kv.rocksdb;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.nio.file.Path;

import static io.webby.util.base.Unchecked.rethrow;

public class RocksDbFactory extends BaseKeyValueFactory {
    @Inject private Settings settings;

    @Override
    public @NotNull <K, V> RocksDbImpl<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options, () -> {
            Path storagePath = settings.storageSettings().keyValueSettingsOrDie().path();
            String filename = settings.getProperty("db.rocksdb.filename.pattern", "rocksdb-%s");
            boolean createIfMissing = settings.getBoolProperty("db.rocksdb.create.if.missing", true);
            boolean paranoidChecks = settings.getBoolProperty("db.rocksdb.paranoid.checks", false);

            String destination = storagePath.resolve(formatFileName(filename, options.name())).toString();
            Codec<K> keyCodec = keyCodecOrDie(options);
            Codec<V> valueCodec = valueCodecOrDie(options);
            try {
                Options rocksOptions = new Options()
                        .setCreateIfMissing(createIfMissing)
                        .setParanoidChecks(paranoidChecks);
                RocksDB db = RocksDB.open(rocksOptions, destination);
                return new RocksDbImpl<>(db, keyCodec, valueCodec);
            } catch (RocksDBException e) {
                return rethrow(e);
            }
        });
    }
}
