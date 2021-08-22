package io.webby.db.kv.swaydb;

import com.google.inject.Inject;
import io.webby.app.AppConfigException;
import io.webby.app.Settings;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.BaseKeyValueFactory;
import io.webby.db.kv.KeyValueDb;
import org.jetbrains.annotations.NotNull;
import swaydb.data.slice.Slice;
import swaydb.java.Map;
import swaydb.java.persistent.PersistentMap;
import swaydb.java.serializers.Serializer;

import java.io.IOException;
import java.nio.file.Path;

public class SwayDbFactory extends BaseKeyValueFactory {
    @Inject private Settings settings;
    @Inject private CodecProvider provider;

    @Override
    public @NotNull <K, V> SwayDb<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            Path storagePath = settings.storagePath();
            String filename = settings.getProperty("db.swaydb.filename.pattern", "swaydb-%s");

            AppConfigException.failIf(!filename.contains("%s"), "The pattern must contain '%%s': %s".formatted(filename));
            Path path = storagePath.resolve(filename.formatted(name));

            Codec<K> keyCodec = provider.getCodecOrDie(key);
            Codec<V> valueCodec = provider.getCodecOrDie(value);

            Map<K, V, Void> map = PersistentMap
                    .functionsOff(path, getSerializer(keyCodec), getSerializer(valueCodec))
                    .get();
            return new SwayDb<>(map);
        });
    }

    @NotNull
    private static <T> Serializer<T> getSerializer(@NotNull Codec<T> codec) {
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
                return codec.readFrom(unbox(array));
            }
        };
    }

    @Override
    public void close() throws IOException {
        cache.values().forEach(KeyValueDb::close);
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
