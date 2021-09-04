package io.webby.db.kv.halodb;

import com.google.inject.Inject;
import com.oath.halodb.HaloDB;
import com.oath.halodb.HaloDBException;
import com.oath.halodb.HaloDBOptions;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

import static io.webby.util.Rethrow.rethrow;

public class HaloDbFactory extends BaseKeyValueFactory {
    // private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private CodecProvider provider;

    @Override
    public @NotNull <K, V> HaloDbImpl<K, V> getInternalDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            Path storagePath = settings.storagePath();

            HaloDBOptions options = new HaloDBOptions();
            // Size of each data file will be 1GB.
            // options.setMaxFileSize(1024 * 1024 * 1024);

            try {
                HaloDB db = HaloDB.open(storagePath.resolve(name).toFile(), options);
                Codec<K> keyCodec = provider.getCodecOrDie(key);
                Codec<V> valueCodec = provider.getCodecOrDie(value);
                return new HaloDbImpl<>(db, keyCodec, valueCodec);
            } catch (HaloDBException e) {
                return rethrow(e);
            }
        });
    }
}
