package io.webby.db.kv.rocksdb;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.nio.file.Path;

import static io.webby.util.Rethrow.rethrow;

public class RocksDbFactory extends BaseKeyValueFactory {
    // private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private CodecProvider provider;

    @Override
    public @NotNull <K, V> RocksDbImpl<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            Path storagePath = settings.storagePath();
            String filename = settings.getProperty("db.rocksdb.filename.pattern", "rocksdb-%s");
            boolean createIfMissing = settings.getBoolProperty("db.rocksdb.create.if.missing", true);
            boolean paranoidChecks = settings.getBoolProperty("db.rocksdb.paranoid.checks", false);

            String destination = storagePath.resolve(formatFileName(filename, name)).toString();
            Codec<K> keyCodec = provider.getCodecOrDie(key);
            Codec<V> valueCodec = provider.getCodecOrDie(value);
            try {
                Options options = new Options()
                        .setCreateIfMissing(createIfMissing)
                        .setParanoidChecks(paranoidChecks);
                RocksDB db = RocksDB.open(options, destination);
                return new RocksDbImpl<>(db, keyCodec, valueCodec);
            } catch (RocksDBException e) {
                return rethrow(e);
            }
        });
    }

    @Override
    public void close() throws IOException {
        cache.values().forEach(KeyValueDb::close);
    }
}
