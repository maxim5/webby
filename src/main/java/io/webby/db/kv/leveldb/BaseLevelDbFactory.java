package io.webby.db.kv.leveldb;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import io.webby.util.Rethrow;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public abstract class BaseLevelDbFactory extends BaseKeyValueFactory {
    // private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final DBFactory dbFactory;

    @Inject private Settings settings;
    @Inject private CodecProvider provider;

    public BaseLevelDbFactory(@NotNull DBFactory dbFactory) {
        this.dbFactory = dbFactory;
    }

    @Override
    public @NotNull <K, V> LevelDbImpl<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            Path storagePath = settings.storagePath();
            String filename = settings.getProperty("db.leveldb.filename.pattern", "leveldb-%s");
            boolean createIfMissing = settings.getBoolProperty("db.leveldb.create.if.missing", true);
            boolean paranoidChecks = settings.getBoolProperty("db.leveldb.paranoid.checks", false);

            File destination = storagePath.resolve(formatFileName(filename, name)).toFile();
            Codec<K> keyCodec = provider.getCodecOrDie(key);
            Codec<V> valueCodec = provider.getCodecOrDie(value);
            try {
                DB db = dbFactory.open(destination, new Options()
                        .createIfMissing(createIfMissing)
                        .paranoidChecks(paranoidChecks));
                return new LevelDbImpl<>(db, keyCodec, valueCodec);
            } catch (IOException e) {
                return Rethrow.rethrow(e);
            }
        });
    }

    @Override
    public void close() throws IOException {
        cache.values().forEach(KeyValueDb::close);
    }
}
