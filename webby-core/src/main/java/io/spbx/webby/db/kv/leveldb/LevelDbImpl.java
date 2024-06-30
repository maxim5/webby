package io.spbx.webby.db.kv.leveldb;

import com.google.common.collect.Streams;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.mu.util.stream.BiStream;
import io.spbx.util.base.Unchecked;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.kv.KeyValueDb;
import io.spbx.webby.db.kv.impl.ByteArrayDb;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.WriteBatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.spbx.util.base.EasyWrappers.MutableInt;
import static io.spbx.util.io.EasyIo.Close.closeQuietly;

public class LevelDbImpl<K, V> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    private final DB db;

    public LevelDbImpl(@NotNull DB db, @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        super(keyCodec, valueCodec);
        this.db = db;
    }

    @Override
    public int size() {
        MutableInt counter = new MutableInt();
        forEachEntry(entry -> counter.value++);
        return counter.value;
    }

    @Override
    public boolean isEmpty() {
        return !withIterator(Iterator::hasNext);
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        return asValue(db.get(fromKey(key)));
    }

    @Override
    public boolean containsKey(@NotNull K key) {
        return db.get(fromKey(key)) != null;
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        byte[] bytes = fromValue(value);
        return forEachEntryEarlyStop(entry -> Arrays.equals(entry.getValue(), bytes));
    }

    @Override
    public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        forEachEntry(entry ->
            action.accept(asKey(entry.getKey()), asValue(entry.getValue()))
        );
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
    public void putAll(@NotNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        writeBatch(batch ->
            entries.forEach(entry -> {
                byte[] key = fromKey(entry.getKey());
                byte[] value = fromValue(entry.getValue());
                batch.put(key, value);
            })
        );
    }

    @Override
    public void putAll(@NotNull Stream<? extends Map.Entry<? extends K, ? extends V>> entries) {
        writeBatch(batch ->
            entries.forEach(entry -> {
               byte[] key = fromKey(entry.getKey());
               byte[] value = fromValue(entry.getValue());
               batch.put(key, value);
            })
        );
    }

    @Override
    public void putAll(@NotNull K @NotNull [] keys, @NotNull V @NotNull [] values) {
        assert keys.length == values.length : "Illegal arrays length: %d vs %d".formatted(keys.length, values.length);
        writeBatch(batch -> {
            for (int i = 0, n = keys.length; i < n; i++) {
                batch.put(fromKey(keys[i]), fromValue(values[i]));
            }
        });
    }

    @Override
    public void putAll(@NotNull Iterable<? extends K> keys, @NotNull Iterable<? extends V> values) {
        writeBatch(batch ->
            BiStream.zip(Streams.stream(keys), Streams.stream(values))
                .mapKeys(this::fromKey)
                .mapValues(this::fromValue)
                .forEach(batch::put)
        );
    }

    @Override
    public void delete(@NotNull K key) {
        db.delete(fromKey(key));
    }

    @Override
    public void removeAll(@NotNull K @NotNull [] keys) {
        writeBatch(batch ->
            Arrays.stream(keys).map(this::fromKey).forEach(batch::delete)
        );
    }

    @Override
    public void removeAll(@NotNull Iterable<K> keys) {
        writeBatch(batch ->
            Streams.stream(keys).map(this::fromKey).forEach(batch::delete)
        );
    }

    @Override
    public void clear() {
        writeBatch(batch ->
            forEachEntry(entry ->
                batch.delete(entry.getKey())
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
            Unchecked.rethrow(e);
        }
    }

    public @NotNull DB internalDb() {
        return db;
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

    @CanIgnoreReturnValue
    private <T> T withIterator(@NotNull Function<DBIterator, T> action) {
        DBIterator iterator = db.iterator();
        try {
            iterator.seekToFirst();
            return action.apply(iterator);
        } finally {
            closeQuietly(iterator);
        }
    }

    private void forEachEntry(@NotNull Consumer<Map.Entry<byte[], byte[]>> action) {
        withIterator(iterator -> {
            while (iterator.hasNext()) {
                action.accept(iterator.next());
            }
            return null;
        });
    }

    @CanIgnoreReturnValue
    private boolean forEachEntryEarlyStop(@NotNull Predicate<Map.Entry<byte[], byte[]>> predicateToStop) {
        return withIterator(iterator -> {
            while (iterator.hasNext()) {
                if (predicateToStop.test(iterator.next())) {
                    return true;
                }
            }
            return false;
        });
    }

    private <T, C extends Collection<T>> @NotNull C collect(@NotNull C destination,
                                                            @NotNull Function<Map.Entry<byte[], byte[]>, T> converter) {
        forEachEntry(entry -> {
            T item = converter.apply(entry);
            destination.add(item);
        });
        return destination;
    }
}
