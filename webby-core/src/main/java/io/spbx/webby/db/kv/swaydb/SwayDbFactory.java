package io.spbx.webby.db.kv.swaydb;

import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.kv.DbOptions;
import io.spbx.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;
import swaydb.data.slice.Slice;
import swaydb.java.Map;
import swaydb.java.persistent.PersistentMap;
import swaydb.java.serializers.Serializer;
import swaydb.persistent.DefaultConfigs;

import java.nio.file.Path;

public class SwayDbFactory extends BaseKeyValueFactory {
    @Override
    public @NotNull <K, V> SwayDb<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options, () -> {
            Path storagePath = settings.storageSettings().keyValueSettingsOrDie().path();
            String filename = settings.getProperty("db.swaydb.filename.pattern", "swaydb-%s");
            int mapSize = settings.getIntProperty("db.swaydb.init.map.size.bytes", 4 << 20);
            int minSegmentSize = settings.getIntProperty("db.swaydb.segment.size.bytes", 2 << 20);
            int checkpointSize = settings.getIntProperty("db.swaydb.appendix.flush.checkpoint.size.bytes", 2 << 20);

            Path path = storagePath.resolve(formatFileName(filename, options.name()));
            Codec<K> keyCodec = keyCodecOrDie(options);
            Codec<V> valueCodec = valueCodecOrDie(options);

            PersistentMap.Config<K, V, Void> config = PersistentMap
                    .functionsOff(path, getSerializer(keyCodec), getSerializer(valueCodec))
                    .setMapSize(mapSize)
                    .setSegmentConfig(DefaultConfigs.segmentConfig(false).copyWithMinSegmentSize(minSegmentSize))
                    .setAppendixFlushCheckpointSize(checkpointSize);
            Map<K, V, Void> map = config.get();
            return new SwayDb<>(map);
        });
    }

    // See https://github.com/simerplaha/SwayDB/issues/308
    private static <T> @NotNull Serializer<T> getSerializer(@NotNull Codec<T> codec) {
        return new Serializer<>() {
            @Override
            public Slice<Byte> write(T data) {
                // return Slice.ofJava(ByteBuffer.wrap(bytes));
                return Slice.ofJava(box(codec.writeToBytes(data)));
            }

            @Override
            public T read(Slice<Byte> slice) {
                // return codec.readFrom(slice.toByteArrayInputStream(), slice.size());
                Byte[] array = (Byte[]) slice.unsafeInnerArray();
                return codec.readFromBytes(unbox(array));
            }
        };
    }

    private static Byte[] box(byte[] bytes) {
        int length = bytes.length;
        Byte[] boxed = new Byte[length];
        for (int i = 0; i < length; i++) {
            boxed[i] = bytes[i];
        }
        return boxed;
    }

    private static byte[] unbox(Byte[] boxed) {
        int length = boxed.length;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = boxed[i];
        }
        return bytes;
    }
}
