package io.webby.db.kv.tupl;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import io.webby.util.AtomicLazy;
import io.webby.util.Rethrow;
import org.cojen.tupl.Database;
import org.cojen.tupl.DatabaseConfig;
import org.cojen.tupl.DurabilityMode;
import org.cojen.tupl.Index;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;

import static io.webby.util.Rethrow.rethrow;

public class TuplFactory extends BaseKeyValueFactory {
    // private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final AtomicLazy<Database> db = new AtomicLazy<>();

    @Inject private Settings settings;
    @Inject private CodecProvider provider;

    @Override
    public @NotNull <K, V> TuplDb<K, V> getInternalDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            Path storagePath = settings.storagePath();
            String filename = settings.getProperty("db.tupl.filename", "tupl.data");

            DatabaseConfig config = new DatabaseConfig()
                    .baseFile(storagePath.resolve(filename).toFile())
                    .cacheSize(100_000_000)
                    .durabilityMode(DurabilityMode.NO_FLUSH);
            Database database = db.lazyGet(Rethrow.Suppliers.rethrow(() -> Database.open(config)));

            try {
                Codec<K> keyCodec = provider.getCodecOrDie(key);
                Codec<V> valueCodec = provider.getCodecOrDie(value);
                Index index = database.openIndex(name);
                return new TuplDb<>(index, database, keyCodec, valueCodec);
            } catch (IOException e) {
                return rethrow(e);
            }
        });
    }
}
