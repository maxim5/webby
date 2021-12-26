package io.webby.db.kv.lmdbjni;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.db.codec.Codec;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import org.fusesource.lmdbjni.ByteUnit;
import org.fusesource.lmdbjni.Constants;
import org.fusesource.lmdbjni.Database;
import org.fusesource.lmdbjni.Env;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class LmdbJniDbFactory extends BaseKeyValueFactory {
    private final Env env;

    @Inject
    public LmdbJniDbFactory(@NotNull InjectorHelper helper) {
        env = helper.getOrDefault(Env.class, this::createDefaultEnv);
    }

    @Override
    public @NotNull <K, V> LmdbJniDb<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options.name(), () -> {
            Codec<K> keyCodec = keyCodecOrDie(options);
            Codec<V> valueCodec = valueCodecOrDie(options);
            Database database = env.openDatabase(options.name(), Constants.CREATE);
            return new LmdbJniDb<>(env, database, keyCodec, valueCodec);
        });
    }

    @Override
    public void close() {
        env.close();
    }

    private @NotNull Env createDefaultEnv(@NotNull Settings settings) {
        Path storagePath = settings.storageSettings().keyValueSettingsOrDie().path();
        long maxMapSize = settings.getLongProperty("db.lmdb-jni.max.map.size.bytes", 64 << 20);
        int maxMapsNum = settings.getIntProperty("db.lmdb-jni.max.maps.num", 32);

        Env env = new Env();
        env.setMapSize(maxMapSize, ByteUnit.BYTES);
        env.setMaxDbs(maxMapsNum);
        env.open(storagePath.toString(), Constants.NOSYNC | Constants.WRITEMAP);
        return env;
    }
}
