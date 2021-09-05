package io.webby.db.kv.oak;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.yahoo.oak.*;
import com.yahoo.oak.common.intbuffer.OakIntBufferComparator;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Level;

import static io.webby.util.EasyCast.castAny;

public class OakFactory extends BaseKeyValueFactory {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private CodecProvider provider;
    @Inject private InjectorHelper helper;

    @Override
    public @NotNull <K, V> OakDb<K, V> getInternalDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            OakRecord<K> keyRecord =
                    Optional.ofNullable(OakKnownTypes.lookupRecord(key))
                    .orElseGet(() -> helper.getOrDefault(new TypeLiteral<OakRecord<K>>() {}, () -> inferOakRecord(key)));

            OakSerializer<V> valueSerializer =
                    Optional.ofNullable(OakKnownTypes.lookupRecord(value))
                    .map(OakRecord::serializer)
                    .orElseGet(() -> {
                        Codec<V> codec = provider.getCodecOrDie(value);
                        return new OakSerializerAdapter<>(codec);
                    });

            int chunkMaxItems = settings.getIntProperty("db.oak.chunk.max.items", 4096);  /*Chunk.MAX_ITEMS_DEFAULT*/
            long memoryCapacity = settings.getLongProperty("db.oak.memory.capacity", 16L << 30);  /*MAX_MEM_CAPACITY*/
            int preferredBlockSize = settings.getIntProperty("db.oak.preferred.block.size.bytes",
                                                             256 << 20);  /*BlocksPool.DEFAULT_BLOCK_SIZE_BYTES*/

            OakMapBuilder<K, V> builder = new OakMapBuilder<>(keyRecord.comparator(),
                                                              keyRecord.serializer(),
                                                              valueSerializer,
                                                              keyRecord.minValue())
                    .setChunkMaxItems(chunkMaxItems)
                    .setMemoryCapacity(memoryCapacity)
                    .setPreferredBlockSize(preferredBlockSize);
            return new OakDb<>(builder.build());
        });
    }

    private <K> @NotNull OakRecord<K> inferOakRecord(@NotNull Class<K> key) {
        Codec<K> keyCodec = provider.getCodecOrDie(key);
        assert keyCodec.size().isFixed() : "Oak requires fixed size codec for the key: %s".formatted(key);
        int byteSize = keyCodec.size().intNumBytes();
        OakSerializer<K> keySerializer = new OakSerializerAdapter<>(keyCodec);

        byte[] zeroBytes = new byte[byteSize];
        K minValue = keyCodec.readFromBytes(zeroBytes);
        log.at(Level.WARNING).log("Using global minimum value for key %s: %s", key, minValue);

        log.at(Level.WARNING).log("Potential performance problem: inferring Oak comparator for key: %s. " +
                                  "Consider injecting OakRecord<> provider", key);
        OakIntBufferComparator bufferComparator = new OakIntBufferComparator(byteSize);
        OakComparator<K> comparator = new OakComparator<>() {
            @Override
            public int compareKeys(K key1, K key2) {
                if (key1 instanceof Comparable<?> comparable) {
                    return comparable.compareTo(castAny(key2));
                }
                return bufferComparator.compareKeys(keyCodec.writeToByteBuffer(key1), keyCodec.writeToByteBuffer(key2));
            }

            @Override
            public int compareSerializedKeys(OakScopedReadBuffer serializedKey1, OakScopedReadBuffer serializedKey2) {
                return bufferComparator.compareSerializedKeys(serializedKey1, serializedKey2);
            }

            @Override
            public int compareKeyAndSerializedKey(K key, OakScopedReadBuffer serializedKey) {
                return bufferComparator.compareKeyAndSerializedKey(keyCodec.writeToByteBuffer(key), serializedKey);
            }
        };

        return new OakRecord<>(comparator, keySerializer, minValue);
    }
}
