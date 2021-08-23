package io.webby.db.kv.leveldbjni;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import io.webby.util.Rethrow;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class LevelDbJniFactory extends BaseKeyValueFactory {
    // private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private CodecProvider provider;

    @Override
    public @NotNull <K, V> LevelDbJni<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            Path storagePath = settings.storagePath();
            String filename = settings.getProperty("db.leveldb-jni.filename.pattern", "leveldb-jni-%s");
            boolean createIfMissing = settings.getBoolProperty("db.leveldb-jni.create.if.missing", true);
            boolean paranoidChecks = settings.getBoolProperty("db.leveldb-jni.paranoid.checks", false);

            File destination = storagePath.resolve(formatFileName(filename, name)).toFile();
            Codec<K> keyCodec = provider.getCodecOrDie(key);
            Codec<V> valueCodec = provider.getCodecOrDie(value);
            try {
                DB db = JniDBFactory.factory.open(destination, new Options()
                        .createIfMissing(createIfMissing)
                        .paranoidChecks(paranoidChecks));
                return new LevelDbJni<>(db, keyCodec, valueCodec);
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
