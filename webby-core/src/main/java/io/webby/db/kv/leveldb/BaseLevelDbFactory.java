package io.webby.db.kv.leveldb;

import io.webby.db.codec.Codec;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import io.spbx.util.base.Unchecked;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public abstract class BaseLevelDbFactory extends BaseKeyValueFactory {
    private final DBFactory dbFactory;

    public BaseLevelDbFactory(@NotNull DBFactory dbFactory) {
        this.dbFactory = dbFactory;
    }

    @Override
    public @NotNull <K, V> LevelDbImpl<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options, () -> {
            Path storagePath = settings.storageSettings().keyValueSettingsOrDie().path();
            String filename = settings.getProperty("db.leveldb.filename.pattern", "leveldb-%s");
            boolean createIfMissing = settings.getBoolProperty("db.leveldb.create.if.missing", true);
            boolean paranoidChecks = settings.getBoolProperty("db.leveldb.paranoid.checks", false);

            File destination = storagePath.resolve(formatFileName(filename, options.name())).toFile();
            Codec<K> keyCodec = keyCodecOrDie(options);
            Codec<V> valueCodec = valueCodecOrDie(options);
            try {
                DB db = dbFactory.open(destination, new Options()
                        .createIfMissing(createIfMissing)
                        .paranoidChecks(paranoidChecks));
                return new LevelDbImpl<>(db, keyCodec, valueCodec);
            } catch (IOException e) {
                return Unchecked.rethrow(e);
            }
        });
    }
}
