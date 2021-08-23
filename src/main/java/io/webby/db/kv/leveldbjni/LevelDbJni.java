package io.webby.db.kv.leveldbjni;

import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.ByteArrayDb;
import io.webby.util.Rethrow;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.WriteBatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.webby.util.EasyIO.Close.closeQuietly;

public class LevelDbJni<K, V> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    private final DB db;

    public LevelDbJni(@NotNull DB db, @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        super(keyCodec, valueCodec);
        this.db = db;
    }

    @Override
    public int size() {
        AtomicInteger counter = new AtomicInteger();
        withIterator(iterator -> iterator.forEachRemaining(entry -> counter.incrementAndGet()));
        return counter.get();
    }

    @Override
    public boolean isEmpty() {
        DBIterator iterator = db.iterator();
        try {
            iterator.seekToFirst();
            return !iterator.hasNext();
        } finally {
            closeQuietly(iterator);
        }
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        return asValue(db.get(fromKey(key)));
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        byte[] bytes = fromValue(value);
        AtomicBoolean found = new AtomicBoolean();
        withIterator(iterator -> {
            while (iterator.hasNext()) {
                Map.Entry<byte[], byte[]> next = iterator.next();
                if (Arrays.equals(next.getValue(), bytes)) {
                    found.set(true);
                    return;
                }
            }
        });
        return found.get();
    }

    @Override
    public @NotNull List<K> keys() {
        return collect(new ArrayList<>(), entry -> asKey(entry.getKey()));
    }

    @Override
    public @NotNull Set<K> keySet() {
        return collect(new HashSet<>(), entry -> asKey(entry.getKey()));
    }

    @Override
    public @NotNull Collection<V> values() {
        return collect(new ArrayList<>(), entry -> asValue(entry.getValue()));
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        return collect(new HashSet<>(), entry -> asMapEntry(entry.getKey(), entry.getValue()));
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        db.put(fromKey(key), fromValue(value));
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        writeBatch(batch -> {
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                byte[] key = fromKey(entry.getKey());
                byte[] value = fromValue(entry.getValue());
                batch.put(key, value);
            }
        });
    }

    @Override
    public void delete(@NotNull K key) {
        db.delete(fromKey(key));
    }

    @Override
    public void clear() {
        writeBatch(batch ->
           withIterator(iterator ->
                iterator.forEachRemaining(entry -> batch.delete(entry.getKey()))
           )
        );
    }

    @Override
    public void flush() {
        // nothing to do
    }

    @Override
    public void close() {
        try {
            db.close();
        } catch (IOException e) {
            Rethrow.rethrow(e);
        }
    }

    private void writeBatch(@NotNull Consumer<WriteBatch> action) {
        WriteBatch batch = db.createWriteBatch();
        try {
            action.accept(batch);
            db.write(batch);
        } finally {
            closeQuietly(batch);
        }
    }

    private void withIterator(@NotNull Consumer<DBIterator> action) {
        DBIterator iterator = db.iterator();
        try {
            iterator.seekToFirst();
            action.accept(iterator);
        } finally {
            closeQuietly(iterator);
        }
    }

    private <T, C extends Collection<T>> @NotNull C collect(@NotNull C destination,
                                                            @NotNull Function<Map.Entry<byte[], byte[]>, T> converter) {
        withIterator(iterator -> iterator.forEachRemaining(entry -> {
            T item = converter.apply(entry);
            destination.add(item);
        }));
        return destination;
    }
}
