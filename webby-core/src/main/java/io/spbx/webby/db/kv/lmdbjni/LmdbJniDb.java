package io.spbx.webby.db.kv.lmdbjni;

import com.google.common.collect.Streams;
import com.google.mu.util.stream.BiStream;
import io.spbx.webby.db.codec.Codec;
import io.spbx.webby.db.kv.KeyValueDb;
import io.spbx.webby.db.kv.impl.ByteArrayDb;
import org.fusesource.lmdbjni.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.spbx.webby.db.kv.impl.KeyValueCommons.streamOf;

public class LmdbJniDb <K, V> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    private final Env env;
    private final Database db;

    public LmdbJniDb(@NotNull Env env, @NotNull Database db, @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        super(keyCodec, valueCodec);
        this.env = env;
        this.db = db;
    }

    @Override
    public int size() {
        return (int) longSize();
    }

    @Override
    public long longSize() {
        return db.stat().ms_entries;
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        return asValue(db.get(fromKey(key)));
    }

    @Override
    public @NotNull List<@Nullable V> getAll(@NotNull K @NotNull [] keys) {
        try (Transaction transaction = env.createReadTransaction()) {
            return Arrays.stream(keys).map(this::fromKey).map(key -> db.get(transaction, key)).map(this::asValue).toList();
        }
    }

    @Override
    public @NotNull List<@Nullable V> getAll(@NotNull Iterable<K> keys) {
        try (Transaction transaction = env.createReadTransaction()) {
            return Streams.stream(keys).map(this::fromKey).map(key -> db.get(transaction, key)).map(this::asValue).toList();
        }
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        byte[] bytes = fromValue(value);
        return iterate(iterator -> streamOf(iterator).map(Entry::getValue).anyMatch(val -> Arrays.equals(val, bytes)));
    }

    @Override
    public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        iterate(iterable -> {
            streamOf(iterable).forEach(entry -> {
                K key = asKey(entry.getKey());
                V value = asValue(entry.getValue());
                action.accept(key, value);
            });
            return "";
        });
    }

    @Override
    public @NotNull Iterable<K> keys() {
        return iterate(iterator -> streamOf(iterator).map(Entry::getKey).map(this::asKey).toList());
    }

    @Override
    public @NotNull Set<K> keySet() {
        return iterate(iterator -> streamOf(iterator).map(Entry::getKey).map(this::asKey).collect(Collectors.toSet()));
    }

    @Override
    public @NotNull Collection<V> values() {
        return iterate(iterator -> streamOf(iterator).map(Entry::getValue).map(this::asValue).toList());
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        return iterate(iterator -> {
            Set<Map.Entry<K, V>> result = new HashSet<>();
            for (Entry entry : iterator.iterable()) {
                result.add(asMapEntry(entry.getKey(), entry.getValue()));
            }
            return result;
        });
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        db.put(fromKey(key), fromValue(value));
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        inWriteTransaction(transaction ->
            map.forEach((key, value) -> putInTransaction(key, value, transaction))
        );
    }

    @Override
    public void putAll(@NotNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        inWriteTransaction(transaction ->
            entries.forEach(entry -> putInTransaction(entry.getKey(), entry.getValue(), transaction))
        );
    }

    @Override
    public void putAll(@NotNull Stream<? extends Map.Entry<? extends K, ? extends V>> entries) {
        inWriteTransaction(transaction ->
            entries.forEach(entry -> putInTransaction(entry.getKey(), entry.getValue(), transaction))
        );
    }

    @Override
    public void putAll(@NotNull K @NotNull [] keys, @NotNull V @NotNull [] values) {
        assert keys.length == values.length : "Illegal arrays length: %d vs %d".formatted(keys.length, values.length);
        inWriteTransaction(transaction ->
            BiStream.zip(Arrays.stream(keys), Arrays.stream(values))
                    .forEach((key, value) -> putInTransaction(key, value, transaction))
        );
    }

    @Override
    public void putAll(@NotNull Iterable<? extends K> keys, @NotNull Iterable<? extends V> values) {
        inWriteTransaction(transaction ->
            BiStream.zip(Streams.stream(keys), Streams.stream(values))
                    .forEach((key, value) -> putInTransaction(key, value, transaction))
        );
    }

    @Override
    public void delete(@NotNull K key) {
        db.delete(fromKey(key));
    }

    @Override
    public void removeAll(@NotNull K @NotNull [] keys) {
        inWriteTransaction(transaction ->
            Arrays.stream(keys).forEach(key -> db.delete(transaction, fromKey(key)))
        );
    }

    @Override
    public void removeAll(@NotNull Iterable<K> keys) {
        inWriteTransaction(transaction ->
            Streams.stream(keys).forEach(key -> db.delete(transaction, fromKey(key)))
        );
    }

    @Override
    public void clear() {
        inWriteTransaction(transaction -> db.drop(transaction, false));
    }

    public void destroy() {
        inWriteTransaction(transaction -> db.drop(transaction, true));
    }

    @Override
    public void flush() {
        env.sync(false);
    }

    @Override
    public void forceFlush() {
        env.sync(true);
    }

    @Override
    public void close() {
        db.close();
    }

    public @NotNull Database internalDb() {
        return db;
    }

    private <T> @NotNull T iterate(@NotNull Function<EntryIterator, T> converter) {
        try (Transaction transaction = env.createReadTransaction()) {
            try (EntryIterator iterator = db.iterate(transaction)) {
                return converter.apply(iterator);
            }
        }
    }

    private void putInTransaction(@NotNull K key, @NotNull V value, @NotNull Transaction transaction) {
        db.put(transaction, fromKey(key), fromValue(value));
    }

    private void inWriteTransaction(@NotNull Consumer<Transaction> consumer) {
        try (Transaction transaction = env.createWriteTransaction()) {
            consumer.accept(transaction);
            transaction.commit();
        }
    }
}
