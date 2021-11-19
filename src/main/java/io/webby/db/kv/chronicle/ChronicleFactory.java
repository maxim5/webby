package io.webby.db.kv.chronicle;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.codec.CodecSize;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import net.openhft.chronicle.hash.serialization.SizeMarshaller;
import net.openhft.chronicle.hash.serialization.SizedReader;
import net.openhft.chronicle.hash.serialization.impl.ExternalizableReader;
import net.openhft.chronicle.hash.serialization.impl.SerializableReader;
import net.openhft.chronicle.hash.serialization.impl.SerializationBuilder;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.logging.Level;

import static io.webby.util.base.Rethrow.Suppliers.rethrow;

public class ChronicleFactory extends BaseKeyValueFactory {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private CodecProvider provider;

    @Override
    public @NotNull <K, V> ChronicleDb<K, V> getInternalDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, rethrow(() -> {
            Path storagePath = settings.storageSettings().storagePath();
            String filename = settings.getProperty("db.chronicle.filename.pattern", "chronicle-%s.data");
            boolean putReturnsNull = settings.getBoolProperty("db.chronicle.put.return.null", false);
            boolean removeReturnsNull = settings.getBoolProperty("db.chronicle.remove.return.null", false);
            boolean skipExitHook = settings.getBoolProperty("db.chronicle.skip.exit.hook", true);
            int replicationId = settings.getIntProperty("db.chronicle.replication.identifier", -1);
            long defaultSize = settings.getLongProperty("db.chronicle.default.size", 1 << 20);

            ChronicleMapBuilder<K, V> builder = ChronicleMap.of(key, value)
                    .entries(defaultSize)
                    .name(name)
                    .putReturnsNull(putReturnsNull)
                    .removeReturnsNull(removeReturnsNull)
                    .skipCloseOnExitHook(skipExitHook);
            if (replicationId > 0) {
                builder.replication((byte) replicationId);
            }

            pickSerialization(key, builder::keySizeMarshaller, builder::averageKeySize, builder::keyMarshaller);
            pickSerialization(value, builder::valueSizeMarshaller, builder::averageValueSize, builder::valueMarshaller);

            File destination = storagePath.resolve(formatFileName(filename, name)).toFile();
            ChronicleMap<K, V> map = builder.createPersistedTo(destination);
            return new ChronicleDb<>(map);
        }));
    }

    private <T> void pickSerialization(@NotNull Class<T> klass,
                                       @NotNull Consumer<SizeMarshaller> onSize,
                                       @NotNull LongConsumer onAverageValueSize,
                                       @NotNull Consumer<BytesReaderWriter<T>> onMarshaller) {
        Codec<T> codec = provider.getCodecOrNull(klass);
        SerializationBuilder<T> serialization = new SerializationBuilder<>(klass);
        CodecSize codecSize = bestSize(serialization, codec);
        switch (codecSize.estimate()) {
            case FIXED -> {
                onSize.accept(SizeMarshaller.constant(codecSize.numBytes()));
                if (!serialization.sizeIsStaticallyKnown) {
                    assert codec != null : "Internal error: %s".formatted(klass);
                    onMarshaller.accept(new BytesReaderWriter<>(codec));
                }
            }
            case AVERAGE, MIN -> {
                assert !serialization.sizeIsStaticallyKnown : "Internal error: %s".formatted(klass);
                onAverageValueSize.accept(codecSize.numBytes());
                if (isDefaultReader(serialization.reader())) {
                    assert codec != null : "Internal error: %s".formatted(klass);
                    onMarshaller.accept(new BytesReaderWriter<>(codec));
                }
            }
        }
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
}
