package io.webby.db.kv.mapdb;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.db.kv.BaseKeyValueFactory;
import io.webby.db.kv.SerializeProvider;
import org.jetbrains.annotations.NotNull;
import org.mapdb.*;
import org.mapdb.serializer.GroupSerializerObjectArray;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;

import static io.webby.util.EasyCast.castAny;

public class MapDbFactory extends BaseKeyValueFactory {
    @Inject private SerializeProvider serializeProvider;

    private final DB db;
    private final MapDbCreator creator;

    @Inject
    public MapDbFactory(@NotNull InjectorHelper helper) {
        db = helper.getOrDefault(DB.class, this::createDefaultMapDB);
        creator = helper.getOrDefault(MapDbCreator.class, () -> (db, name, key, value) -> null);
    }

    @NotNull
    public <K, V> MapDbImpl<K, V> getDb(@NotNull String name, @NotNull Class<K> key, @NotNull Class<V> value) {
        return cacheIfAbsent(name, () -> {
            DB.Maker<HTreeMap<?, ?>> customMaker = creator.getMaker(db, name, key, value);

            DB.Maker<HTreeMap<K, V>> maker;
            if (customMaker != null) {
                maker = castAny(customMaker);
            } else {
                maker = db.hashMap(name, pickSerializer(key), pickSerializer(value));
            }

            HTreeMap<K, V> map = maker.createOrOpen();
            return new MapDbImpl<>(db, map);
        });
    }

    @Override
    public void close() throws IOException {
        db.close();
    }

    @NotNull
    private DB createDefaultMapDB(@NotNull Settings settings) {
        // Consider options: hash or tree
        Path storagePath = settings.storagePath();
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
    @NotNull
    private <T> Serializer<T> pickSerializer(@NotNull Class<T> klass) {
        Serializer<?> outOfBox = OUT_OF_BOX.get(klass);
        if (outOfBox != null) {
            return castAny(outOfBox);
        }

        io.webby.db.kv.Serializer<T> ownCustom = serializeProvider.getSerializer(klass);
        if (ownCustom != null) {
            return new GroupSerializerObjectArray<>() {
                @Override
                public void serialize(@NotNull DataOutput2 out, @NotNull T value) throws IOException {
                    ownCustom.writeTo(out, value);
                }

                @Override
                public T deserialize(@NotNull DataInput2 input, int available) throws IOException {
                    return ownCustom.readFrom(new DataInput2.DataInputToStream(input), available);
                }
            };
        }

        return castAny(Serializer.JAVA);
    }
}
