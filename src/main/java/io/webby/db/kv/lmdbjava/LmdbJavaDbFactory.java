package io.webby.db.kv.lmdbjava;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.BaseKeyValueFactory;
import io.webby.db.kv.KeyValueDb;
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
        env = helper.getOrDefault(Env.class, this::createDefaultEnv);
    }

    @Override
    public @NotNull <K, V> KeyValueDb<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        Codec<K> keyCodec = codecProvider.getCodecFor(key);
        assert keyCodec != null : "No codec found the key class: %s".formatted(key);

        Codec<V> valueCodec = codecProvider.getCodecFor(value);
        assert valueCodec != null : "No codec found the value class: %s".formatted(value);

        Dbi<ByteBuffer> db = env.openDbi(name, MDB_CREATE);
        return new LmdbJavaDb<>(env, db, keyCodec, valueCodec);
    }

    @Override
    public void close() throws IOException {
        env.close();
    }

    private @NotNull Env<ByteBuffer> createDefaultEnv(@NotNull Settings settings) {
        Path storagePath = settings.storagePath();
        long maxMapSize = settings.getLongProperty("db.lmdbjava.max.map.size.bytes", 64 << 20);
        int maxMapsNum = settings.getIntProperty("db.lmdbjava.max.maps.num", 32);

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
