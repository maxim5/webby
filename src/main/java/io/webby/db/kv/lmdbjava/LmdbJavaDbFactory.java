package io.webby.db.kv.lmdbjava;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lmdbjava.DbiFlags.MDB_CREATE;

public class LmdbJavaDbFactory extends BaseKeyValueFactory {
    private final Env<ByteBuffer> env;

    @Inject private CodecProvider codecProvider;

    @Inject
    public LmdbJavaDbFactory(@NotNull InjectorHelper helper) {
        env = helper.getOrDefault(new TypeLiteral<>() {}, this::createDefaultEnv);
    }

    @Override
    public @NotNull <K, V> LmdbJavaDb<K, V> getInternalDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            Codec<K> keyCodec = codecProvider.getCodecOrDie(key);
            Codec<V> valueCodec = codecProvider.getCodecOrDie(value);
            Dbi<ByteBuffer> db = env.openDbi(name, MDB_CREATE);
            return new LmdbJavaDb<>(env, db, keyCodec, valueCodec);
        });
    }

    @Override
    public void close() throws IOException {
        env.close();
    }

    private @NotNull Env<ByteBuffer> createDefaultEnv(@NotNull Settings settings) {
        Path storagePath = settings.storageSettings().keyValueSettingsOrDie().path();
        long maxMapSize = settings.getLongProperty("db.lmdb-java.max.map.size.bytes", 64 << 20);
        int maxMapsNum = settings.getIntProperty("db.lmdb-java.max.maps.num", 32);

        return Env.create()
                // LMDB also needs to know how large our DB might be. Over-estimating is OK.
                .setMapSize(maxMapSize)
                // LMDB also needs to know how many DBs (Dbi) we want to store in this Env.
                .setMaxDbs(maxMapsNum)
                // The same path can be concurrently opened and used in different processes,
                // but do not open the same path twice in the same process at the same time.
                .open(storagePath.toFile());
    }
}
