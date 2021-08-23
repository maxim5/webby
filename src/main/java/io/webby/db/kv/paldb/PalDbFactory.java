package io.webby.db.kv.paldb;

import com.google.inject.Inject;
import com.linkedin.paldb.api.Configuration;
import com.linkedin.paldb.api.PalDB;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class PalDbFactory extends BaseKeyValueFactory {
    // private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private CodecProvider provider;

    @Override
    public @NotNull <K, V> PalDbImpl<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            Path storagePath = settings.storagePath();
            String filename = settings.getProperty("db.paldb.filename.pattern", "paldb-%s");

            String path = storagePath.resolve(formatFileName(filename, name)).toString();
            Codec<K> keyCodec = provider.getCodecOrDie(key);
            Codec<V> valueCodec = provider.getCodecOrDie(value);
            Configuration config = PalDB.newConfiguration();
            return new PalDbImpl<>(path, config, keyCodec, valueCodec);
        });
    }
}
