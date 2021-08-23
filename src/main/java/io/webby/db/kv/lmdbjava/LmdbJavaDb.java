package io.webby.db.kv.lmdbjava;

import com.google.common.collect.Streams;
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
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public boolean containsValue(@NotNull V value) {
        ByteBuffer buffer = directBufferFromValue(value);
        return iterate(iterable -> Streams.stream(iterable)
                .map(CursorIterable.KeyVal::val)
                .anyMatch(val -> val.equals(buffer)));
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

    private <T> @NotNull T iterate(@NotNull Function<CursorIterable<ByteBuffer>, T> converter) {
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            try (CursorIterable<ByteBuffer> iterable = db.iterate(txn)) {
                return converter.apply(iterable);
            }
        }
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        ByteBuffer bufferKey = directBufferFromKey(key);
        ByteBuffer bufferValue = directBufferFromValue(value);
        db.put(bufferKey, bufferValue);
    }

    @Override
    public void delete(@NotNull K key) {
        ByteBuffer buffer = directBufferFromKey(key);
        db.delete(buffer);
    }

    @Override
    public void clear() {
        try (Txn<ByteBuffer> txn = env.txnWrite()) {
            db.drop(txn, false);
            txn.commit();
        }
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
}
