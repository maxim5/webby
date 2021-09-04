package io.webby.db.kv.tupl;

import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.ByteArrayDb;
import io.webby.util.Counting.BoolFlag;
import io.webby.util.func.ThrowConsumer;
import io.webby.util.func.ThrowFunction;
import org.cojen.tupl.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

import static io.webby.db.kv.impl.KeyValueCommons.quiet;
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
        return quiet(() -> index.count(null, true, null, true));
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
    public void delete(@NotNull K key) {
        inWriteTransaction(transaction -> index.delete(transaction, fromKey(key)));
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
        quiet(() -> database.deleteIndex(index));
    }

    @Override
    public void flush() {
        quiet(database::flush);
    }

    @Override
    public void forceFlush() {
        quiet(database::sync);
    }

    @Override
    public void close() {
        quiet(index::close);
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
            quiet(transaction::exit);
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
            quiet(transaction::exit);
        }
    }
}
