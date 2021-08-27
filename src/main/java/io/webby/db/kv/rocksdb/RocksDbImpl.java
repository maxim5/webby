package io.webby.db.kv.rocksdb;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.mu.util.stream.BiStream;
import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.ByteArrayDb;
import io.webby.util.Counting;
import io.webby.util.Counting.IntCount;
import io.webby.util.Rethrow.Consumers;
import io.webby.util.func.ThrowConsumer;
import io.webby.util.func.ThrowFunction;
import io.webby.util.func.ThrowPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rocksdb.*;
import swaydb.data.util.Counter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.webby.util.Rethrow.rethrow;

public class RocksDbImpl<K, V> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    private static final FlushOptions FLUSH_OPTIONS = new FlushOptions();
    private static final WriteOptions WRITE_OPTIONS = new WriteOptions();

    private final RocksDB db;

    public RocksDbImpl(@NotNull RocksDB db, @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        super(keyCodec, valueCodec);
        this.db = db;
    }

    @Override
    public int size() {
        IntCount counter = new IntCount();
        forEachEntry(iterator -> counter.val++);
        return counter.val;
    }

    @Override
    public boolean isEmpty() {
        return !withIterator(AbstractRocksIterator::isValid);
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        try {
            return asValue(db.get(fromKey(key)));
        } catch (RocksDBException e) {
            return rethrow(e);
        }
    }

    @Override
    public @NotNull List<@Nullable V> getAll(@NotNull K @NotNull [] keys) {
        try {
            return keys.length == 0 ? List.of() :
                db.multiGetAsList(Arrays.stream(keys).map(this::fromKey).toList()).stream().map(this::asValue).toList();
        } catch (RocksDBException e) {
            return rethrow(e);
        }
    }

    @Override
    public @NotNull List<@Nullable V> getAll(@NotNull Iterable<K> keys) {
        try {
            return Iterables.isEmpty(keys) ? List.of() :
                db.multiGetAsList(Streams.stream(keys).map(this::fromKey).toList()).stream().map(this::asValue).toList();
        } catch (RocksDBException e) {
            return rethrow(e);
        }
    }

    @Override
    public boolean containsKey(@NotNull K key) {
        try {
            return db.get(fromKey(key)) != null;
        } catch (RocksDBException e) {
            return rethrow(e);
        }
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        byte[] bytes = fromValue(value);
        return forEachEntryEarlyStop(iterator -> Arrays.equals(iterator.value(), bytes));
    }

    @Override
    public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        forEachEntry(iterator ->
            action.accept(asKey(iterator.key()), asValue(iterator.value()))
        );
    }

    @Override
    public @NotNull Iterable<K> keys() {
        return collect(new ArrayList<>(), it -> asKey(it.key()));
    }

    @Override
    public @NotNull Set<K> keySet() {
        return collect(new HashSet<>(), it -> asKey(it.key()));
    }

    @Override
    public @NotNull Collection<V> values() {
        return collect(new ArrayList<>(), entry -> asValue(entry.value()));
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        return collect(new HashSet<>(), entry -> asMapEntry(entry.key(), entry.value()));
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        try {
            db.put(fromKey(key), fromValue(value));
        } catch (RocksDBException e) {
            rethrow(e);
        }
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
    public void putAll(@NotNull Iterable<Map.Entry<? extends K, ? extends V>> entries) {
        writeBatch(batch -> {
            for (Map.Entry<? extends K, ? extends V> entry : entries) {
               byte[] key = fromKey(entry.getKey());
               byte[] value = fromValue(entry.getValue());
               batch.put(key, value);
            }
        });
    }

    @Override
    public void putAll(@NotNull Stream<Map.Entry<? extends K, ? extends V>> entries) {
        writeBatch(batch ->
            entries.forEach(Consumers.rethrow(entry -> {
               byte[] key = fromKey(entry.getKey());
               byte[] value = fromValue(entry.getValue());
               batch.put(key, value);
            }))
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
                   .forEach(Consumers.rethrow(batch::put))
        );
    }

    @Override
    public void delete(@NotNull K key) {
        try {
            db.delete(fromKey(key));
        } catch (RocksDBException e) {
            rethrow(e);
        }
    }

    @Override
    public void removeAll(@NotNull K @NotNull [] keys) {
        writeBatch(batch -> {
           for (K key : keys) {
               batch.delete(fromKey(key));
           }
        });
    }

    @Override
    public void removeAll(@NotNull Iterable<K> keys) {
        writeBatch(batch -> {
            for (K key : keys) {
                batch.delete(fromKey(key));
            }
        });
    }

    @Override
    public void clear() {
        writeBatch(batch ->
            forEachEntry(iterator ->
                batch.delete(iterator.key())
            )
        );
    }

    @Override
    public void flush() {
        try {
            db.flush(FLUSH_OPTIONS);
        } catch (RocksDBException e) {
            rethrow(e);
        }
    }

    @Override
    public void close() {
        db.close();
    }

    public @NotNull RocksDB internalDb() {
        return db;
    }

    private void writeBatch(@NotNull ThrowConsumer<WriteBatch, RocksDBException> action) {
        try (WriteBatch batch = new WriteBatch()) {
            action.accept(batch);
            db.write(WRITE_OPTIONS, batch);
        } catch (RocksDBException e) {
            rethrow(e);
        }
    }

    @CanIgnoreReturnValue
    private <T> T withIterator(@NotNull ThrowFunction<RocksIterator, T, RocksDBException> action) {
        try (RocksIterator iterator = db.newIterator()) {
            iterator.seekToFirst();
            return action.apply(iterator);
        } catch (RocksDBException e) {
            return rethrow(e);
        }
    }

    private void forEachEntry(@NotNull ThrowConsumer<RocksIterator, RocksDBException> action) {
        withIterator(iterator -> {
            for (; iterator.isValid(); iterator.next()) {
                action.accept(iterator);
            }
            return null;
        });
    }

    @CanIgnoreReturnValue
    private boolean forEachEntryEarlyStop(@NotNull ThrowPredicate<RocksIterator, RocksDBException> predicateToStop) {
        return withIterator(iterator -> {
            for (; iterator.isValid(); iterator.next()) {
                if (predicateToStop.test(iterator)) {
                    return true;
                }
            }
            return false;
        });
    }

    private <T, C extends Collection<T>> @NotNull C collect(@NotNull C destination,
                                                            @NotNull Function<RocksIterator, T> converter) {
        forEachEntry(iterator -> {
            T item = converter.apply(iterator);
            destination.add(item);
        });
        return destination;
    }
}
