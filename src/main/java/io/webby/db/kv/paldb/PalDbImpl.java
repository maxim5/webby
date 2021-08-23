package io.webby.db.kv.paldb;

import com.google.common.collect.Streams;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;
import com.linkedin.paldb.api.StoreWriter;
import com.linkedin.paldb.impl.ReaderImpl;
import com.linkedin.paldb.impl.StorageReader;
import io.webby.db.codec.Codec;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.ByteArrayDb;
import io.webby.util.Rethrow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PalDbImpl<K, V> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    private final String path;
    private final AtomicInteger count = new AtomicInteger();

    private StorageReader reader = null;
    private StoreWriter writer = null;

    private static final Field storageField = internalStorage();

    public PalDbImpl(@NotNull String path, @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        super(keyCodec, valueCodec);
        this.path = path;
    }

    @Override
    public int size() {
        return reader().getKeyCount();
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        try {
            return asValue(reader().get(fromKey(key)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        return streamOf(reader().iterator())
                .map(Map.Entry::getValue)
                .map(this::asValueNotNull)
                .anyMatch(val -> val.equals(value));
    }

    @Override
    public @NotNull Iterable<K> keys() {
        return streamOf(reader().keys()).map(Map.Entry::getKey).map(this::asKey).toList();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return streamOf(reader().keys()).map(Map.Entry::getKey).map(this::asKey).collect(Collectors.toSet());
    }

    @Override
    public @NotNull Collection<V> values() {
        return streamOf(reader().iterator()).map(Map.Entry::getValue).map(this::asValue).toList();
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        return streamOf(reader().iterator())
                .map(e -> asMapEntry(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        writer().put(fromKey(key), fromValue(value));
    }

    @Override
    public void delete(@NotNull K key) {
        // not implemented yet
    }

    @Override
    public synchronized void clear() {
        close();
        writer = PalDB.createWriter(getFile(count.incrementAndGet()));
    }

    @Override
    public void flush() {
    }

    @Override
    public synchronized void close() {
        if (reader != null) {
            closeQuiet(reader);
            reader = null;
        }
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }

    private @NotNull StoreWriter writer() {
        if (writer != null) {
            return writer;
        }

        synchronized (this) {
            writer = PalDB.createWriter(getFile(count.incrementAndGet()));
            if (reader != null) {
                reader.iterator().forEachRemaining(entry -> writer.put(entry.getKey(), entry.getValue()));
                closeQuiet(reader);
                reader = null;
            }
            return writer;
        }
    }

    private @NotNull StorageReader reader() {
        if (reader != null) {
            return reader;
        }
        synchronized (this) {
            if (writer != null) {
                writer.close();
                writer = null;
            }
            return reader = extractStorageFromInstance(PalDB.createReader(getFile(count.get())));
        }
    }

    private static @NotNull Field internalStorage() {
        try {
            Field field = ReaderImpl.class.getDeclaredField("storage");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return Rethrow.rethrow(e);
        }
    }

    private static @NotNull StorageReader extractStorageFromInstance(@NotNull StoreReader reader) {
        try {
            return (StorageReader) storageField.get(reader);
        } catch (IllegalAccessException e) {
            return Rethrow.rethrow(e);
        }
    }

    private @NotNull File getFile(int index) {
        return new File("%s.%08X.paldb".formatted(path, index));
    }

    private static void closeQuiet(@NotNull StorageReader storageReader) {
        try {
            storageReader.close();
        } catch (IOException e) {
            Rethrow.rethrow(e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static <T> @NotNull Stream<T> streamOf(@NotNull Iterator<T> iterator) {
        return Streams.stream(iterator);
    }

    public static void main(String[] args) {
        PalDbImpl<Integer, String> pal = new PalDbImpl<>(".data/tmp", CodecProvider.INT_CODEC, CodecProvider.STRING_CODEC);

        pal.set(1, "foo");
        pal.set(2, "bar");

        System.out.println(pal.get(1));
        System.out.println(pal.get(2));
        System.out.println(pal.keys());

        pal.set(3, "foobar");

        System.out.println(pal.keys());
        System.out.println(pal.get(1));
        System.out.println(pal.get(2));
        System.out.println(pal.get(3));
    }
}
