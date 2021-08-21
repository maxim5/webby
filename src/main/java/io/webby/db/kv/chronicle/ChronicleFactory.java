package io.webby.db.kv.chronicle;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.app.AppConfigException;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.codec.CodecSize;
import io.webby.db.kv.BaseKeyValueFactory;
import io.webby.db.kv.KeyValueDb;
import net.openhft.chronicle.hash.serialization.*;
import net.openhft.chronicle.hash.serialization.impl.ExternalizableReader;
import net.openhft.chronicle.hash.serialization.impl.SerializableReader;
import net.openhft.chronicle.hash.serialization.impl.SerializationBuilder;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;

import static io.webby.util.Rethrow.Suppliers.rethrow;

public class ChronicleFactory extends BaseKeyValueFactory {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private CodecProvider provider;

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
            if (replicationId > 0) {
                builder.replication((byte) replicationId);
            }

            {
                Codec<K> keyCodec = provider.getCodecFor(key);
                SerializationBuilder<K> keySerialization = new SerializationBuilder<>(key);
                CodecSize keySize = bestSize(keySerialization, keyCodec);
                switch (keySize.estimate()) {
                    case FIXED -> {
                        builder.keySizeMarshaller(SizeMarshaller.constant(keySize.numBytes()));
                        if (!keySerialization.sizeIsStaticallyKnown) {
                            builder.keyMarshaller(new BytesReaderWriter<>(Objects.requireNonNull(keyCodec)));
                        }
                    }
                    case AVERAGE, MIN -> {
                        assert !keySerialization.sizeIsStaticallyKnown;
                        builder.averageKeySize(keySize.numBytes());
                        if (isDefaultReader(keySerialization.reader())) {
                            builder.keyMarshaller(new BytesReaderWriter<>(Objects.requireNonNull(keyCodec)));
                        }
                    }
                }
            }

            {
                Codec<V> valueCodec = provider.getCodecFor(value);
                SerializationBuilder<V> valueSerialization = new SerializationBuilder<>(value);
                CodecSize valueSize = bestSize(valueSerialization, valueCodec);
                switch (valueSize.estimate()) {
                    case FIXED -> {
                        builder.valueSizeMarshaller(SizeMarshaller.constant(valueSize.numBytes()));
                        if (!valueSerialization.sizeIsStaticallyKnown) {
                            builder.valueMarshaller(new BytesReaderWriter<>(Objects.requireNonNull(valueCodec)));
                        }
                    }
                    case AVERAGE, MIN -> {
                        assert !valueSerialization.sizeIsStaticallyKnown;
                        builder.averageValueSize(valueSize.numBytes());
                        if (isDefaultReader(valueSerialization.reader())) {
                            builder.valueMarshaller(new BytesReaderWriter<>(Objects.requireNonNull(valueCodec)));
                        }
                    }
                }
            }

            ChronicleMap<K, V> map = builder.createPersistedTo(destination);
            return new ChronicleDb<>(map);
        }));
    }

    private static <T> @NotNull CodecSize bestSize(@NotNull SerializationBuilder<T> builder, @Nullable Codec<T> codec) {
        Class<T> klass = builder.tClass;

        if (builder.sizeIsStaticallyKnown) {
            long constantSize = builder.constantSize();
            if (codec == null) {
                log.at(Level.CONFIG).log("Size inconsistency for %s: %d vs null", klass, constantSize);
            } else if (codec.size().estimate() != CodecSize.Estimate.FIXED) {
                log.at(Level.CONFIG).log("Size inconsistency for %s: %d vs %s", klass, constantSize, codec.size());
            } else if (codec.size().numBytes() != constantSize) {
                log.at(Level.CONFIG).log("Size inconsistency for %s: %d vs %s", klass, constantSize, codec.size());
            } else {
                return codec.size();
            }
            return CodecSize.fixed(constantSize);
        }

        return Objects.requireNonNull(codec).size();
    }

    private static <T> boolean isDefaultReader(@NotNull SizedReader<T> reader) {
        return reader instanceof SerializableReader || reader instanceof ExternalizableReader;
    }

    @Override
    public void close() throws IOException {
        cache.values().forEach(KeyValueDb::close);
    }
}
