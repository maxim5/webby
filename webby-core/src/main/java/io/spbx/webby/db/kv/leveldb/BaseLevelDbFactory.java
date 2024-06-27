package io.spbx.webby.db.kv.leveldb;

import io.spbx.util.base.Unchecked;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.kv.DbOptions;
import io.spbx.webby.db.kv.impl.BaseKeyValueFactory;
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
            String filename = settings.get("db.leveldb.filename.pattern", "leveldb-%s");
            boolean createIfMissing = settings.getBool("db.leveldb.create.if.missing", true);
            boolean paranoidChecks = settings.getBool("db.leveldb.paranoid.checks", false);

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
