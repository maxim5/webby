package io.spbx.webby.db.kv.paldb;

import com.google.inject.Inject;
import com.linkedin.paldb.api.Configuration;
import com.linkedin.paldb.api.PalDB;
import io.spbx.webby.app.Settings;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.codec.CodecProvider;
import io.spbx.webby.db.kv.DbOptions;
import io.spbx.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class PalDbFactory extends BaseKeyValueFactory {
    @Inject private Settings settings;
    @Inject private CodecProvider provider;

    @Override
    public @NotNull <K, V> PalDbImpl<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options, () -> {
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
