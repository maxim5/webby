package io.webby.db.kv.rocksdb;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.ByteArrayDb;
import io.webby.util.func.ThrowConsumer;
import io.webby.util.func.ThrowFunction;
import io.webby.util.func.ThrowPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.rocksdb.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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
        AtomicInteger counter = new AtomicInteger();
        forEachEntry(iterator -> counter.incrementAndGet());
        return counter.get();
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
    public boolean containsValue(@NotNull V value) {
        byte[] bytes = fromValue(value);
        return forEachEntryEarlyStop(iterator -> Arrays.equals(iterator.value(), bytes));
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
    public void delete(@NotNull K key) {
        try {
            db.delete(fromKey(key));
        } catch (RocksDBException e) {
            rethrow(e);
        }
    }

    @Override
    public void clear() {
        writeBatch(batch ->
            forEachEntry(iterator ->
                batch.delete(iterator.key())
            )
        );
    }

    public @NotNull RocksDB internalDb() {
        return db;
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
