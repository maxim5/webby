package io.webby.db.kv.tupl;

import com.google.common.collect.Streams;
import com.google.mu.util.stream.BiStream;
import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.ByteArrayDb;
import io.webby.util.collect.EasyIterables;
import io.webby.util.EasyPrimitives.BoolFlag;
import io.webby.util.Rethrow.Consumers;
import io.webby.util.func.ThrowConsumer;
import io.webby.util.func.ThrowFunction;
import org.cojen.tupl.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.webby.util.Rethrow.Runnables.runRethrow;
import static io.webby.util.Rethrow.Suppliers.runRethrow;
import static io.webby.util.Rethrow.rethrow;

public class TuplDb<K, V> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    private final Index index;
    private final Database database;

    public TuplDb(@NotNull Index index, @NotNull Database database, @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        super(keyCodec, valueCodec);
        this.index = index;
        this.database = database;
    }

    @Override
    public int size() {
        return (int) longSize();
    }

    @Override
    public long longSize() {
        return runRethrow(() -> index.count(null, true, null, true));
    }

    @Override
    public boolean isEmpty() {
        try {
            return index.isEmpty();
        } catch (IOException e) {
            return rethrow(e);
        }
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        return inReadTransaction(transaction -> asValue(index.load(transaction, fromKey(key))));
    }

    @Override
    public @NotNull List<@Nullable V> getAll(@NotNull K @NotNull [] keys) {
        return inReadTransaction(transaction -> {
            ArrayList<@Nullable V> result = new ArrayList<>(keys.length);
            for (K key : keys) {
                V value = asValue(index.load(transaction, fromKey(key)));
                result.add(value);
            }
            return result;
        });
    }

    @Override
    public @NotNull List<@Nullable V> getAll(@NotNull Iterable<K> keys) {
        return inReadTransaction(transaction -> {
            ArrayList<@Nullable V> result = new ArrayList<>(EasyIterables.estimateSizeInt(keys, 5));
            for (K key : keys) {
                V value = asValue(index.load(transaction, fromKey(key)));
                result.add(value);
            }
            return result;
        });
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        BoolFlag found = new BoolFlag();
        byte[] bytes = fromValue(value);
        iterate(cursor -> {
            byte[] currentBytes = cursor.value();
            if (Arrays.equals(bytes, currentBytes)) {
                found.flag = true;
                return false;
            }
            return currentBytes != null;
        });
        return found.flag;
    }

    @Override
    public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        iterate(cursor -> {
            byte[] key = cursor.key();
            byte[] value = cursor.value();
            if (key != null) {
                action.accept(asKey(key), asValue(value));
            }
            return key != null;
        });
    }

    @Override
    public @NotNull Set<K> keySet() {
        HashSet<K> set = new HashSet<>();
        iterate(cursor -> {
            byte[] key = cursor.key();
            if (key != null) {
                set.add(asKey(key));
            }
            return key != null;
        });
        return set;
    }

    @Override
    public @NotNull Collection<V> values() {
        ArrayList<V> list = new ArrayList<>();
        iterate(cursor -> {
            byte[] value = cursor.value();
            if (value != null) {
                list.add(asValue(value));
            }
            return value != null;
        });
        return list;
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        HashSet<Map.Entry<K, V>> set = new HashSet<>();
        iterate(cursor -> {
            byte[] key = cursor.key();
            byte[] value = cursor.value();
            if (key != null) {
                set.add(asMapEntry(key, value));
            }
            return key != null;
        });
        return set;
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        inWriteTransaction(transaction -> index.store(transaction, fromKey(key), fromValue(value)));
    }

    @Override
    public @Nullable V put(@NotNull K key, @NotNull V value) {
        return fromWriteTransaction(transaction -> asValue(index.exchange(transaction, fromKey(key), fromValue(value))));
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        putAll(map.entrySet());
    }

    @Override
    public void putAll(@NotNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        inWriteTransaction(transaction -> {
            for (Map.Entry<? extends K, ? extends V> entry : entries) {
                index.store(transaction, fromKey(entry.getKey()), fromValue(entry.getValue()));
            }
        });
    }

    @Override
    public void putAll(@NotNull Stream<? extends Map.Entry<? extends K, ? extends V>> entries) {
        inWriteTransaction(transaction ->
            entries.forEach(Consumers.rethrow(entry ->
                index.store(transaction, fromKey(entry.getKey()), fromValue(entry.getValue()))
            ))
        );
    }

    @Override
    public void putAll(@NotNull K @NotNull [] keys, @NotNull V @NotNull [] values) {
        assert keys.length == values.length : "Illegal arrays length: %d vs %d".formatted(keys.length, values.length);
        inWriteTransaction(transaction -> {
            for (int i = 0, n = keys.length; i < n; i++) {
                index.store(transaction, fromKey(keys[i]), fromValue(values[i]));
            }
        });
    }

    @Override
    public void putAll(@NotNull Iterable<? extends K> keys, @NotNull Iterable<? extends V> values) {
        inWriteTransaction(transaction -> {
            BiStream.zip(Streams.stream(keys), Streams.stream(values))
                    .mapKeys(this::fromKey)
                    .mapValues(this::fromValue)
                    .forEach(Consumers.rethrow((k, v) -> index.store(transaction, k, v)));
        });
    }

    @Override
    public void delete(@NotNull K key) {
        inWriteTransaction(transaction -> index.delete(transaction, fromKey(key)));
    }

    @Override
    public void removeAll(@NotNull K @NotNull [] keys) {
        inWriteTransaction(transaction -> {
            for (K key : keys) {
                index.delete(transaction, fromKey(key));
            }
        });
    }

    @Override
    public void removeAll(@NotNull Iterable<K> keys) {
        inWriteTransaction(transaction -> {
            for (K key : keys) {
                index.delete(transaction, fromKey(key));
            }
        });
    }

    @Override
    public void clear() {
        inWriteTransaction(transaction -> {
            for (K key : keys()) {
                index.delete(transaction, fromKey(key));
            }
        });
    }

    public void destroy() {
        runRethrow(() -> database.deleteIndex(index));
    }

    @Override
    public void flush() {
        runRethrow(database::flush);
    }

    @Override
    public void forceFlush() {
        runRethrow(database::sync);
    }

    @Override
    public void close() {
        runRethrow(index::close);
    }

    public @NotNull Index internalIndex() {
        return index;
    }

    public @NotNull Database internalDatabase() {
        return database;
    }

    private void iterate(@NotNull Predicate<Cursor> action) {
        inReadTransaction(transaction -> {
            try (Cursor cursor = index.newCursor(transaction)) {
                cursor.first();
                while (action.test(cursor)) {
                    cursor.next();
                }
            }
            return null;
        });
    }

    private <T> T inReadTransaction(@NotNull ThrowFunction<Transaction, T, IOException> action) {
        Transaction transaction = index.newTransaction(DurabilityMode.NO_REDO);
        try {
            return action.apply(transaction);
        } catch (IOException e) {
            return rethrow(e);
        } finally {
            runRethrow(transaction::exit);
        }
    }

    private void inWriteTransaction(@NotNull ThrowConsumer<Transaction, IOException> action) {
        Transaction transaction = index.newTransaction(DurabilityMode.NO_SYNC);
        try {
            action.accept(transaction);
            transaction.commit();
        } catch (IOException e) {
            rethrow(e);
        } finally {
            runRethrow(transaction::exit);
        }
    }

    private <T> T fromWriteTransaction(@NotNull ThrowFunction<Transaction, T, IOException> action) {
        Transaction transaction = index.newTransaction(DurabilityMode.NO_SYNC);
        try {
            T result = action.apply(transaction);
            transaction.commit();
            return result;
        } catch (IOException e) {
            return rethrow(e);
        } finally {
            runRethrow(transaction::exit);
        }
    }
}
