package io.webby.db.kv.paldb;

import com.linkedin.paldb.api.Configuration;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreWriter;
import com.linkedin.paldb.impl.StorageReader;
import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.ByteArrayDb;
import io.webby.util.base.Unchecked;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static io.webby.db.kv.impl.KeyValueCommons.streamOf;
import static io.webby.util.base.EasyCast.castAny;

public class PalDbImpl<K, V> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    private final String path;
    private final Configuration config;
    private final AtomicInteger count = new AtomicInteger();

    private StorageReader reader = null;
    private Map<K, V> writeCache = null;

    public PalDbImpl(@NotNull String path, @NotNull Configuration config,
                     @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        super(keyCodec, valueCodec);
        this.path = path;
        this.config = config;
    }

    @Override
    public int size() {
        return reader().getKeyCount();
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        if (writeCache != null) {
            return writeCache.get(key);
        }
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
                .anyMatch(value::equals);
    }

    @Override
    public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        streamOf(reader().iterator())
                .map(e -> asMapEntry(e.getKey(), e.getValue()))
                .forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
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
        writeCache().put(key, value);
    }

    @Override
    public @Nullable V put(@NotNull K key, @NotNull V value) {
        return writeCache().put(key, value);
    }

    @Override
    public void delete(@NotNull K key) {
        writeCache().remove(key);
    }

    @Override
    public @Nullable V remove(@NotNull K key) {
        return writeCache().remove(key);
    }

    @Override
    public void clear() {
        if (reader != null) {
            closeQuiet(reader);
            reader = null;
        }
        writeCache = null;
        createEmptyPalDb();
    }

    @Override
    public void flush() {
        if (writeCache != null) {
            dumpCacheToNextRevision();
            writeCache = null;
        }
    }

    @Override
    public void close() {
        flush();
        if (reader != null) {
            closeQuiet(reader);
            reader = null;
        }
    }

    public @NotNull StorageReader internalReader() {
        return reader();
    }

    private @NotNull StorageReader reader() {
        if (reader != null) {
            assert writeCache == null;
            return reader;
        }

        if (writeCache != null) {
            dumpCacheToNextRevision();
            writeCache = null;
        } else {
            createEmptyPalDb();
        }
        return reader = instantiateStorageReader(getFile(count.get()));
    }

    private @NotNull Map<K, V> writeCache() {
        if (writeCache != null) {
            assert reader == null;
            return writeCache;
        }

        if (reader != null) {
            writeCache = new HashMap<>(size());
            reader.iterator().forEachRemaining(entry -> writeCache.put(asKey(entry.getKey()), asValue(entry.getValue())));
            closeQuiet(reader);
            reader = null;
        } else {
            writeCache = new HashMap<>();
        }
        return writeCache;
    }

    private void createEmptyPalDb() {
        PalDB.createWriter(getFile(count.incrementAndGet())).close();
    }

    private void dumpCacheToNextRevision() {
        StoreWriter writer = PalDB.createWriter(getFile(count.incrementAndGet()));
        try {
            for (Map.Entry<K, V> entry : writeCache.entrySet()) {
                writer.put(fromKey(entry.getKey()), fromValue(entry.getValue()));
            }
        } finally {
            writer.close();
        }
    }

    private static final Constructor<StorageReader> storageReader = accessStorageReaderConstructor();

    private static @NotNull Constructor<StorageReader> accessStorageReaderConstructor() {
        Constructor<StorageReader> constructor = castAny(StorageReader.class.getDeclaredConstructors()[0]);
        constructor.setAccessible(true);
        return constructor;
    }

    private @NotNull StorageReader instantiateStorageReader(@NotNull File file) {
        try {
            return storageReader.newInstance(config, file);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            return Unchecked.rethrow(e);
        }
    }

    private @NotNull File getFile(int index) {
        return new File("%s.%08x.paldb".formatted(path, index));
    }

    private static void closeQuiet(@NotNull StorageReader storageReader) {
        try {
            storageReader.close();
        } catch (IOException e) {
            Unchecked.rethrow(e);
        }
    }
}
