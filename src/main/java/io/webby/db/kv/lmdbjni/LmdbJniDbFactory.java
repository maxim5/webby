package io.webby.db.kv.lmdbjni;

import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.BaseKeyValueFactory;
import org.fusesource.lmdbjni.ByteUnit;
import org.fusesource.lmdbjni.Constants;
import org.fusesource.lmdbjni.Database;
import org.fusesource.lmdbjni.Env;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

public class LmdbJniDbFactory extends BaseKeyValueFactory {
    private final Env env;

    @Inject
    private CodecProvider codecProvider;

    @Inject
    public LmdbJniDbFactory(@NotNull InjectorHelper helper) {
        env = helper.getOrDefault(Env.class, this::createDefaultEnv);
    }

    @Override
    public @NotNull <K, V> LmdbJniDb<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        Codec<K> keyCodec = codecProvider.getCodecFor(key);
        assert keyCodec != null : "No codec found the key class: %s".formatted(key);

        Codec<V> valueCodec = codecProvider.getCodecFor(value);
        assert valueCodec != null : "No codec found the value class: %s".formatted(value);

        Database database = env.openDatabase(name, Constants.CREATE);
        return new LmdbJniDb<>(env, database, keyCodec, valueCodec);
    }

    @Override
    public void close() throws IOException {
        env.close();
    }

    private @NotNull Env createDefaultEnv(@NotNull Settings settings) {
        Path storagePath = settings.storagePath();
        long maxMapSize = settings.getLongProperty("db.lmdbjni.max.map.size.bytes", 64 << 20);
        int maxMapsNum = settings.getIntProperty("db.lmdbjni.max.maps.num", 32);

        Env env = new Env();
        env.setMapSize(maxMapSize, ByteUnit.BYTES);
        env.setMaxDbs(maxMapsNum);
        env.open(storagePath.toString(), Constants.NOSYNC | Constants.WRITEMAP);
        return env;
    }
}
