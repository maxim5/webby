package io.webby.db.kv.redis;

import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import com.google.mu.util.stream.BiStream;
import io.webby.db.codec.Codec;
import io.webby.db.kv.KeyValueDb;
import io.webby.db.kv.impl.ByteArrayDb;
import io.spbx.util.collect.EasyIterables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JedisDb<K, V> extends ByteArrayDb<K, V> implements KeyValueDb<K, V> {
    private static final byte[] CURSOR_START = "0".getBytes();
    private static final IntFunction<byte[][]> NEW_ARRAY = byte[][]::new;

    private final Jedis jedis;
    private final Supplier<RedisInfo> info = Suppliers.memoize(() -> RedisInfo.parseFrom(internalDb().info()));

    private final byte[] namespace;
    private final byte[] namespacePattern;

    public JedisDb(@NotNull Jedis jedis, @Nullable String name, @NotNull Codec<K> keyCodec, @NotNull Codec<V> valueCodec) {
        super(keyCodec, valueCodec);
        this.jedis = jedis;

        if (name != null) {
            namespace = "%s:".formatted(name).getBytes();
            namespacePattern = "%s:*".formatted(name).getBytes();
        } else {
            namespace = new byte[0];
            namespacePattern = "*".getBytes();
        }
    }

    @Override
    public int size() {
        return (int) longSize();
    }

    @Override
    public long longSize() {
        if (namespace.length == 0) {
            return jedis.dbSize();
        }
        return jedis.keys(namespacePattern).size();
    }

    @Override
    public @Nullable V get(@NotNull K key) {
        return asValue(jedis.get(fromKey(key)));
    }

    @Override
    public @NotNull List<@Nullable V> getAll(@NotNull K @NotNull [] keys) {
        return keys.length == 0 ? List.of() : jedis.mget(fromKeys(keys)).stream().map(this::asValue).toList();
    }

    @Override
    public @NotNull List<@Nullable V> getAll(@NotNull Iterable<K> keys) {
        return Iterables.isEmpty(keys) ? List.of() : jedis.mget(fromKeys(keys)).stream().map(this::asValue).toList();
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
            ScanResult<byte[]> scanResult = jedis.scan(cursor, new ScanParams().match(namespacePattern));
            List<byte[]> chunkKeys = scanResult.getResult();
            List<byte[]> chunkValues = chunkKeys.isEmpty() ? List.of() : jedis.mget(chunkKeys.toArray(NEW_ARRAY));
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
        return jedis.keys(namespacePattern).stream().map(this::asKey).toList();
    }

    @Override
    public @NotNull Set<K> keySet() {
        return jedis.keys(namespacePattern).stream().map(this::asKey).collect(Collectors.toSet());
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
              return nil
            end
        """.getBytes(), 1, keyBytes, valueBytes);
        assert result == null || result instanceof byte[] : "Unexpected eval result: %s".formatted(result);
        return asValue((byte[]) result);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        byte[][] keyVals = new byte[2 * map.size()][];
        int i = 0;
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            keyVals[i++] = fromKey(entry.getKey());
            keyVals[i++] = fromValue(entry.getValue());
        }
        if (keyVals.length > 0)
            jedis.mset(keyVals);
    }

    @Override
    public void putAll(@NotNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        List<byte[]> keyVals = new ArrayList<>(2 * EasyIterables.estimateSizeInt(entries, 5));
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            keyVals.add(fromKey(entry.getKey()));
            keyVals.add(fromValue(entry.getValue()));
        }
        if (!keyVals.isEmpty())
            jedis.mset(keyVals.toArray(NEW_ARRAY));
    }

    @Override
    public void putAll(@NotNull Stream<? extends Map.Entry<? extends K, ? extends V>> entries) {
        List<byte[]> keyVals = new ArrayList<>();
        entries.forEach(entry -> {
            keyVals.add(fromKey(entry.getKey()));
            keyVals.add(fromValue(entry.getValue()));
        });
        if (!keyVals.isEmpty())
            jedis.mset(keyVals.toArray(NEW_ARRAY));
    }

    @Override
    public void putAll(@NotNull K @NotNull [] keys, @NotNull V @NotNull [] values) {
        int n = keys.length;
        int m = values.length;
        assert n == m : "Illegal arrays length: %d vs %d".formatted(n, m);
        byte[][] keyVals = new byte[2 * n][];
        for (int i = 0; i < n; i++) {
            keyVals[i * 2] = fromKey(keys[i]);
            keyVals[i * 2 + 1] = fromValue(values[i]);
        }
        if (keyVals.length > 0)
            jedis.mset(keyVals);
    }

    @Override
    public void putAll(@NotNull Iterable<? extends K> keys, @NotNull Iterable<? extends V> values) {
        List<? extends K> keysList = EasyIterables.asList(keys);
        List<? extends V> valuesList = EasyIterables.asList(values);

        int n = keysList.size();
        int m = valuesList.size();
        assert n == m : "Illegal iterables lengths: %d vs %d".formatted(n, m);
        byte[][] keyVals = new byte[2 * n][];
        for (int i = 0; i < n; i++) {
            keyVals[i * 2] = fromKey(keysList.get(i));
            keyVals[i * 2 + 1] = fromValue(valuesList.get(i));
        }
        if (keyVals.length > 0)
            jedis.mset(keyVals);
    }

    @Override
    public void delete(@NotNull K key) {
        jedis.del(fromKey(key));
    }

    @Override
    public @Nullable V remove(@NotNull K key) {
        if (info.get().isVersionAfter("6.2.0")) {
            return asValue(jedis.getDel(fromKey(key)));
        }
        return KeyValueDb.super.remove(key);
    }

    @Override
    public void removeAll(@NotNull K @NotNull [] keys) {
        if (keys.length > 0)
            jedis.del(fromKeys(keys));
    }

    @Override
    public void removeAll(@NotNull Iterable<K> keys) {
        if (!Iterables.isEmpty(keys))
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

    public @NotNull Jedis internalDb() {
        return jedis;
    }

    @Override
    protected byte @NotNull [] fromKey(@NotNull K key) {
        return keyCodec.writeToBytes(namespace, key);
    }

    @Override
    protected @NotNull K asKeyNotNull(byte @NotNull [] bytes) {
        return keyCodec.readFromBytes(namespace.length, bytes);
    }

    private byte[][] fromKeys(@NotNull K @NotNull [] keys) {
        return Arrays.stream(keys).map(this::fromKey).toArray(NEW_ARRAY);
    }

    private byte[][] fromKeys(@NotNull Iterable<K> keys) {
        return Streams.stream(keys).map(this::fromKey).toArray(NEW_ARRAY);
    }
}
