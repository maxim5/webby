package io.webby.db.kv.paldb;

import com.google.inject.Inject;
import com.linkedin.paldb.api.Configuration;
import com.linkedin.paldb.api.PalDB;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class PalDbFactory extends BaseKeyValueFactory {
    @Inject private Settings settings;
    @Inject private CodecProvider provider;

    @Override
    public @NotNull <K, V> PalDbImpl<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options.name(), () -> {
            Path storagePath = settings.storageSettings().keyValueSettingsOrDie().path();
            String filename = settings.getProperty("db.paldb.filename.pattern", "paldb-%s");

            String path = storagePath.resolve(formatFileName(filename, options.name())).toString();
            Codec<K> keyCodec = keyCodecOrDie(options);
            Codec<V> valueCodec = valueCodecOrDie(options);
            Configuration config = PalDB.newConfiguration();
            return new PalDbImpl<>(path, config, keyCodec, valueCodec);
        });
    }
}
