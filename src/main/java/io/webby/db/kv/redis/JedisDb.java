package io.webby.db.kv.redis;

import com.google.common.collect.Streams;
import com.google.mu.util.stream.BiStream;
import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.ByteArrayDb;
import io.webby.util.EasyList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanResult;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JedisDb<K, V> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    private static final byte[] ALL_KEYS_PATTERN = "*".getBytes();
    private static final byte[] CURSOR_START = "0".getBytes();

    private static final IntFunction<byte[][]> NEW_ARRAY = byte[][]::new;

    private final Jedis jedis;

    // TODO: namespace
    public JedisDb(@NotNull Jedis jedis, @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        super(keyCodec, valueCodec);
        this.jedis = jedis;
    }

    @Override
    public int size() {
        return (int) longSize();
    }

    @Override
    public long longSize() {
        return jedis.dbSize();
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        return asValue(jedis.get(fromKey(key)));  // nil?
    }

    @Override
    public @NotNull List<@Nullable V> getAll(@NotNull K @NotNull [] keys) {
        return jedis.mget(fromKeys(keys)).stream().map(this::asValue).toList();
    }

    @Override
    public @NotNull List<@Nullable V> getAll(@NotNull Iterable<K> keys) {
        return jedis.mget(fromKeys(keys)).stream().map(this::asValue).toList();
    }

    @Override
    public boolean containsKey(@NotNull K key) {
        return jedis.exists(fromKey(key));
    }

    @Override
    public boolean containsValue(@NotNull V value) {
        AtomicBoolean found = new AtomicBoolean();
        forEach((key, val) -> {
            // Optimize: early stop
            if (value.equals(val)) {
                found.set(true);
            }
        });
        return found.get();
    }

    @Override
    public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        byte[] cursor = CURSOR_START;
        while (true) {
            ScanResult<byte[]> scanResult = jedis.scan(cursor);
            List<byte[]> chunkKeys = scanResult.getResult();
            List<byte[]> chunkValues = jedis.mget(chunkKeys.toArray(NEW_ARRAY));
            BiStream.zip(chunkKeys, chunkValues).mapKeys(this::asKey).mapValues(this::asValue).forEach(action);
            if (scanResult.isCompleteIteration()) {
                return;
            }
            cursor = scanResult.getCursorAsBytes();
        }
    }

    @Override
    public @NotNull Iterable<K> keys() {
        // Optimize: iterate chunks via scan
        return jedis.keys(ALL_KEYS_PATTERN).stream().map(this::asKey).toList();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return jedis.keys(ALL_KEYS_PATTERN).stream().map(this::asKey).collect(Collectors.toSet());
    }

    @Override
    public @NotNull Collection<V> values() {
        ArrayList<V> list = new ArrayList<>(size());
        forEach((key, value) -> list.add(value));
        return list;
    }

    @Override
    public @NotNull Set<Map.Entry<K, V>> entrySet() {
        HashSet<Map.Entry<K, V>> hashSet = new HashSet<>();
        forEach((key, value) -> hashSet.add(new AbstractMap.SimpleEntry<>(key, value)));
        return hashSet;
    }

    @Override
    public void set(@NotNull K key, @NotNull V value) {
        jedis.set(fromKey(key), fromValue(value));
    }

    @Override
    public @Nullable V put(@NotNull K key, @NotNull V value) {
        return asValue(jedis.getSet(fromKey(key), fromValue(value)));
    }

    @Override
    public @Nullable V putIfAbsent(@NotNull K key, @NotNull V value) {
        byte[] keyBytes = fromKey(key);
        byte[] valueBytes = fromValue(value);

        // Based on https://github.com/nathan7/redis-require/blob/master/getsetnx.redis.lua
        Object result = jedis.eval("""
            -- EVAL getsetnx 1 key val
            local key = KEYS[1]
            local val = ARGV[1]
            
            if redis.call('exists', key) == 1 then
              return redis.call('get', key)
            else
              redis.call('set', key, val)
              return val
            end
        """.getBytes(), 1, keyBytes, valueBytes);
        assert result instanceof byte[];
        return asValue((byte[]) result);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        byte[][] keyVals = new byte[map.size()][];
        int i = 0;
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            keyVals[i++] = fromKey(entry.getKey());
            keyVals[i++] = fromValue(entry.getValue());
        }
        jedis.mset(keyVals);
    }

    @Override
    public void putAll(@NotNull Iterable<Map.Entry<? extends K, ? extends V>> entries) {
        List<byte[]> keyVals = new ArrayList<>();
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            keyVals.add(fromKey(entry.getKey()));
            keyVals.add(fromValue(entry.getValue()));
        }
        jedis.mset(keyVals.toArray(NEW_ARRAY));
    }

    @Override
    public void putAll(@NotNull Stream<Map.Entry<? extends K, ? extends V>> entries) {
        List<byte[]> keyVals = new ArrayList<>();
        entries.forEach(entry -> {
            keyVals.add(fromKey(entry.getKey()));
            keyVals.add(fromValue(entry.getValue()));
        });
        jedis.mset(keyVals.toArray(NEW_ARRAY));
    }

    @Override
    public void putAll(@NotNull K @NotNull [] keys, @NotNull V @NotNull [] values) {
        /*
        byte[][] array = Streams.concat(Arrays.stream(keys).map(this::fromKey),
                                        Arrays.stream(values).map(this::fromValue))
                .toArray(NEW_ARRAY);
        */

        int n = keys.length;
        int m = values.length;
        assert n == m : "Illegal arrays length: %d vs %d".formatted(n, m);
        byte[][] keyVals = new byte[n][];
        for (int i = 0; i < n; i++) {
            keyVals[i * 2] = fromKey(keys[i]);
            keyVals[i * 2 + 1] = fromValue(values[i]);
        }
        jedis.mset(keyVals);
    }

    @Override
    public void putAll(@NotNull Iterable<? extends K> keys, @NotNull Iterable<? extends V> values) {
        List<? extends K> keysList = EasyList.asList(keys);
        List<? extends V> valuesList = EasyList.asList(values);

        int n = keysList.size();
        int m = valuesList.size();
        assert n == m : "Illegal input lengths: %d vs %d".formatted(n, m);
        byte[][] keyVals = new byte[n][];
        for (int i = 0; i < n; i++) {
            keyVals[i * 2] = fromKey(keysList.get(i));
            keyVals[i * 2 + 1] = fromValue(valuesList.get(i));
        }
        jedis.mset(keyVals);
    }

    @Override
    public void delete(@NotNull K key) {
        jedis.del(fromKey(key));
    }

    @Override
    public @Nullable V remove(@NotNull K key) {
        return asValue(jedis.getDel(fromKey(key)));
    }

    @Override
    public void removeAll(@NotNull K @NotNull [] keys) {
        jedis.del(fromKeys(keys));
    }

    @Override
    public void removeAll(@NotNull Iterable<K> keys) {
        jedis.del(fromKeys(keys));
    }

    @Override
    public void clear() {
        jedis.flushDB();
    }

    @Override
    public void flush() {
        // not available
    }

    @Override
    public void close() {
        jedis.close();
    }

    private byte[][] fromKeys(@NotNull K @NotNull [] keys) {
        return Arrays.stream(keys).map(this::fromKey).toArray(NEW_ARRAY);
    }

    private byte[][] fromKeys(@NotNull Iterable<K> keys) {
        return Streams.stream(keys).map(this::fromKey).toArray(NEW_ARRAY);
    }
}
