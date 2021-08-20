package io.webby.db.kv.chronicle;

import com.google.inject.Inject;
import io.webby.app.AppConfigException;
import io.webby.app.Settings;
import io.webby.db.kv.BaseKeyValueFactory;
import io.webby.db.kv.KeyValueDb;
import net.openhft.chronicle.hash.serialization.SizeMarshaller;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static io.webby.util.Rethrow.Suppliers.rethrow;

public class ChronicleFactory extends BaseKeyValueFactory {
    @Inject private Settings settings;

    @Override
    public @NotNull <K, V> ChronicleDb<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, rethrow(() -> {
            Path storagePath = settings.storagePath();
            String filename = settings.getProperty("db.chronicle.filename.pattern", "chronicle-%s.data");
            boolean putReturnsNull = settings.getBoolProperty("db.chronicle.put.return.null", false);
            boolean removeReturnsNull = settings.getBoolProperty("db.chronicle.remove.return.null", false);
            boolean skipExitHook = settings.getBoolProperty("db.chronicle.skip.exit.hook", true);
            int replicationId = settings.getIntProperty("db.chronicle.replication.identifier", -1);
            long defaultSize = settings.getLongProperty("db.chronicle.default.size", 1 << 20);

            AppConfigException.failIf(!filename.contains("%s"), "The pattern must contain '%%s': %s".formatted(filename));
            File destination = storagePath.resolve(filename.formatted(name)).toFile();

            ChronicleMapBuilder<K, V> builder = ChronicleMap.of(key, value)
                    .entries(defaultSize)
                    .name(name)
                    .putReturnsNull(putReturnsNull)
                    .removeReturnsNull(removeReturnsNull)
                    .skipCloseOnExitHook(skipExitHook);
            // TODO: temp stub for Long keys/values
            if (key == Long.class) {
                builder.keySizeMarshaller(SizeMarshaller.constant(8));
            } else {
                builder.averageKeySize(8);
            }
            if (value == Long.class) {
                builder.valueSizeMarshaller(SizeMarshaller.constant(8));
            } else {
                builder.averageValueSize(8);
            }
            if (replicationId > 0) {
                builder.replication((byte) replicationId);
            }

            ChronicleMap<K, V> map = builder.createPersistedTo(destination);
            return new ChronicleDb<>(map);
        }));
    }

    @Override
    public void close() throws IOException {
        cache.values().forEach(KeyValueDb::close);
    }
}
