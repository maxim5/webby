package io.webby.db.kv.mapdb;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.db.codec.Codec;
import io.webby.db.kv.DbOptions;
import io.webby.db.kv.impl.BaseKeyValueFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapdb.*;
import org.mapdb.serializer.GroupSerializerObjectArray;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;

import static io.webby.util.base.EasyCast.castAny;

public class MapDbFactory extends BaseKeyValueFactory {
    private final DB db;
    private final MapDbCreator creator;

    @Inject
    public MapDbFactory(@NotNull InjectorHelper helper) {
        db = helper.getOrDefault(DB.class, this::createDefaultMapDB);
        creator = helper.getOrDefault(MapDbCreator.class, () -> (db, options) -> null);
    }

    @Override
    public @NotNull <K, V> MapDbImpl<K, V> getInternalDb(@NotNull DbOptions<K, V> options) {
        return cacheIfAbsent(options.name(), () -> {
            DB.Maker<HTreeMap<?, ?>> customMaker = creator.getMaker(db, options);

            DB.Maker<HTreeMap<K, V>> maker;
            if (customMaker != null) {
                maker = castAny(customMaker);
            } else {
                maker = db.hashMap(options.name(),
                                   pickSerializer(options.key(), keyCodecOrNull(options)),
                                   pickSerializer(options.value(), valueCodecOrNull(options)));
            }

            HTreeMap<K, V> map = maker.createOrOpen();
            return new MapDbImpl<>(db, map);
        });
    }

    @Override
    public void close() {
        db.close();
    }

    private @NotNull DB createDefaultMapDB(@NotNull Settings settings) {
        // Consider options: hash or tree
        Path storagePath = settings.storageSettings().keyValueSettingsOrDie().path();
        String filename = settings.getProperty("db.mapdb.filename", "mapdb.data");
        boolean checksumEnabled = settings.getBoolProperty("db.mapdb.checksum.enabled", true);
        boolean concurrencyEnabled = settings.getBoolProperty("db.mapdb.concurrency.enabled", true);
        boolean transactionsEnabled = settings.getBoolProperty("db.mapdb.transactions.enabled", false);
        long allocateStartSize = settings.getLongProperty("db.mapdb.start.size", 0);

        DBMaker.Maker maker = DBMaker.fileDB(storagePath.resolve(filename).toFile());
        if (!checksumEnabled) {
            maker = maker.checksumHeaderBypass();
        }
        if (!concurrencyEnabled) {
            maker = maker.concurrencyDisable();
        }
        if (transactionsEnabled) {
            maker = maker.transactionEnable();
        }

        return maker.allocateStartSize(allocateStartSize).make();
    }

    // More settings: choose default for string, etc?
    private static final ImmutableMap<Class<?>, Serializer<?>> OUT_OF_BOX =
            ImmutableMap.<Class<?>, Serializer<?>>builder()
                    .put(String.class, Serializer.STRING)
                    .put(Byte.class, Serializer.BYTE)
                    .put(byte[].class, Serializer.BYTE_ARRAY)
                    .put(Character.class, Serializer.CHAR)
                    .put(char[].class, Serializer.CHAR_ARRAY)
                    .put(Integer.class, Serializer.INTEGER)
                    .put(int[].class, Serializer.INT_ARRAY)
                    .put(Long.class, Serializer.LONG)
                    .put(long[].class, Serializer.LONG_ARRAY)
                    .put(Float.class, Serializer.FLOAT)
                    .put(float[].class, Serializer.FLOAT_ARRAY)
                    .put(Double.class, Serializer.DOUBLE)
                    .put(double[].class, Serializer.DOUBLE_ARRAY)
                    .put(Boolean.class, Serializer.BOOLEAN)
                    .put(Date.class, Serializer.DATE)
            .build();

    // More flexibility:
    // - force custom
    // - force different out-of-box (e.g. elsa, or packed)
    private <T> @NotNull Serializer<T> pickSerializer(@NotNull Class<T> klass, @Nullable Codec<T> codec) {
        Serializer<?> outOfBox = OUT_OF_BOX.get(klass);
        if (outOfBox != null) {
            return castAny(outOfBox);
        }

        if (codec != null) {
            return new GroupSerializerObjectArray<>() {
                @Override
                public void serialize(@NotNull DataOutput2 out, @NotNull T value) throws IOException {
                    codec.writeTo(out, value);
                }

                @Override
                public T deserialize(@NotNull DataInput2 input, int available) throws IOException {
                    return codec.readFrom(new DataInput2.DataInputToStream(input), available);
                }
            };
        }

        return castAny(Serializer.JAVA);
    }
}
