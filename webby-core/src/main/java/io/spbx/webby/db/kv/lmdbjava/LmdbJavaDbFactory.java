package io.spbx.webby.db.kv.lmdbjava;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import io.spbx.webby.app.Settings;
import io.spbx.webby.common.InjectorHelper;
import io.spbx.webby.common.Lifetime;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.kv.DbOptions;
import io.spbx.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import static org.lmdbjava.DbiFlags.MDB_CREATE;

public class LmdbJavaDbFactory extends BaseKeyValueFactory {
    private final Env<ByteBuffer> env;

    @Inject
    public LmdbJavaDbFactory(@NotNull InjectorHelper helper, @NotNull Lifetime lifetime) {
        env = helper.getOrDefault(new TypeLiteral<>() {}, this::createDefaultEnv);
        lifetime.onTerminate(env::close);
    }

    @Override
    public @NotNull <K, V> LmdbJavaDb<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options, () -> {
            Codec<K> keyCodec = keyCodecOrDie(options);
            Codec<V> valueCodec = valueCodecOrDie(options);
            Dbi<ByteBuffer> db = env.openDbi(options.name(), MDB_CREATE);
            return new LmdbJavaDb<>(env, db, keyCodec, valueCodec);
        });
    }

    private @NotNull Env<ByteBuffer> createDefaultEnv(@NotNull Settings settings) {
        Path storagePath = settings.storageSettings().keyValueSettingsOrDie().path();
        long maxMapSize = settings.getLong("db.lmdb-java.max.map.size.bytes", 64 << 20);
        int maxMapsNum = settings.getInt("db.lmdb-java.max.maps.num", 32);

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
