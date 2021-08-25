package io.webby.db.kv.lmdbjava;

import com.google.common.collect.Streams;
import com.google.mu.util.stream.BiStream;
import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.ByteArrayDb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lmdbjava.CursorIterable;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LmdbJavaDb<K, V> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    private final Env<ByteBuffer> env;
    private final Dbi<ByteBuffer> db;

    public LmdbJavaDb(@NotNull Env<ByteBuffer> env, @NotNull Dbi<ByteBuffer> db,
                      @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
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
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            return db.stat(txn).entries;
        }
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            ByteBuffer bufferKey = directBufferFromKey(key);
            ByteBuffer bufferValue = db.get(txn, bufferKey);
            return asValue(bufferValue);
        }
    }

    @Override
    public @NotNull List<@Nullable V> getAll(@NotNull K @NotNull [] keys) {
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            return Arrays.stream(keys).map(this::directBufferFromKey).map(key -> db.get(txn, key)).map(this::asValue).toList();
        }
    }

    @Override
    public @NotNull List<@Nullable V> getAll(@NotNull Iterable<K> keys) {
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            return Streams.stream(keys).map(this::directBufferFromKey).map(key -> db.get(txn, key)).map(this::asValue).toList();
        }
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        ByteBuffer buffer = directBufferFromValue(value);
        return iterate(iterable -> Streams.stream(iterable)
                .map(CursorIterable.KeyVal::val)
                .anyMatch(val -> val.equals(buffer)));
    }

    @Override
    public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        iterate(iterable -> {
            Streams.stream(iterable).forEach(entry -> {
                K key = asKey(entry.key());
                V value = asValue(entry.val());
                action.accept(key, value);
            });
            return null;
        });
    }

    @Override
    public @NotNull Iterable<K> keys() {
        return iterate(iterable -> Streams.stream(iterable).map(CursorIterable.KeyVal::key).map(this::asKey).toList());
    }

    @Override
    public @NotNull Set<K> keySet() {
        return iterate(iterable -> Streams.stream(iterable)
                .map(CursorIterable.KeyVal::key)
                .map(this::asKey)
                .collect(Collectors.toSet()));
    }

    @Override
    public @NotNull Collection<V> values() {
        return iterate(iterable -> Streams.stream(iterable).map(CursorIterable.KeyVal::val).map(this::asValue).toList());
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        return iterate(iterable -> {
            Set<Map.Entry<K, V>> result = new HashSet<>();
            for (CursorIterable.KeyVal<ByteBuffer> entry : iterable) {
                result.add(asMapEntry(entry.key(), entry.val()));
            }
            return result;
        });
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        inWriteTxn(txn -> putInTxn(key, value, txn));
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        inWriteTxn(txn ->
            map.forEach((key, value) -> putInTxn(key, value, txn))
        );
    }

    @Override
    public void putAll(@NotNull Iterable<Map.Entry<? extends K, ? extends V>> entries) {
        inWriteTxn(txn ->
            entries.forEach(entry -> putInTxn(entry.getKey(), entry.getValue(), txn))
        );
    }

    @Override
    public void putAll(@NotNull Stream<Map.Entry<? extends K, ? extends V>> entries) {
        inWriteTxn(txn ->
            entries.forEach(entry -> putInTxn(entry.getKey(), entry.getValue(), txn))
        );
    }

    @Override
    public void putAll(@NotNull K @NotNull [] keys, @NotNull V @NotNull [] values) {
        assert keys.length == values.length : "Illegal arrays length: %d vs %d".formatted(keys.length, values.length);
        inWriteTxn(txn ->
            BiStream.zip(Arrays.stream(keys), Arrays.stream(values)).forEach((key, value) -> putInTxn(key, value, txn))
        );
    }

    @Override
    public void putAll(@NotNull Iterable<? extends K> keys, @NotNull Iterable<? extends V> values) {
        inWriteTxn(txn ->
            BiStream.zip(Streams.stream(keys), Streams.stream(values)).forEach((key, value) -> putInTxn(key, value, txn))
        );
    }

    @Override
    public void delete(@NotNull K key) {
        ByteBuffer buffer = directBufferFromKey(key);
        db.delete(buffer);
    }

    @Override
    public void removeAll(@NotNull K @NotNull [] keys) {
        inWriteTxn(txn ->
            Arrays.stream(keys).forEach(key -> db.delete(txn, directBufferFromKey(key)))
        );
    }

    @Override
    public void removeAll(@NotNull Iterable<K> keys) {
        inWriteTxn(txn ->
            Streams.stream(keys).forEach(key -> db.delete(txn, directBufferFromKey(key)))
        );
    }

    @Override
    public void clear() {
        inWriteTxn(txn -> db.drop(txn, false));
    }

    public void destroy() {
        inWriteTxn(txn -> db.drop(txn, true));
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

    public @NotNull Dbi<ByteBuffer> internalDb() {
        return db;
    }

    private <T> @NotNull T iterate(@NotNull Function<CursorIterable<ByteBuffer>, T> converter) {
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            try (CursorIterable<ByteBuffer> iterable = db.iterate(txn)) {
                return converter.apply(iterable);
            }
        }
    }

    private void putInTxn(@NotNull K key, @NotNull V value, @NotNull Txn<ByteBuffer> txn) {
        ByteBuffer bufferKey = directBufferFromKey(key);
        ByteBuffer bufferValue = directBufferFromValue(value);
        db.put(txn, bufferKey, bufferValue);
    }

    private void inWriteTxn(@NotNull Consumer<Txn<ByteBuffer>> consumer) {
        try (Txn<ByteBuffer> txn = env.txnWrite()) {
            consumer.accept(txn);
            txn.commit();
        }
    }
}
