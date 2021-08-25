package io.webby.db.kv;

import com.google.common.collect.Streams;
import com.google.mu.util.stream.BiStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// More Map methods: compute
public interface KeyValueDb<K, V> extends Closeable {
    // DB size

    int size();

    default long longSize() {
        return size();
    }

    default boolean isEmpty() {
        return size() == 0;
    }

    default boolean isNotEmpty() {
        return !isEmpty();
    }

    // Get single

    @Nullable V get(@NotNull K key);

    default @NotNull V getOrDefault(@NotNull K key, @NotNull V def) {
        V value = get(key);
        return value != null ? value : def;
    }

    default @NotNull V getOrCompute(@NotNull K key, @NotNull Supplier<V> supplier) {
        V value = get(key);
        return value != null ? value : supplier.get();
    }

    default @NotNull Optional<V> getOptional(@NotNull K key) {
        return Optional.ofNullable(get(key));
    }

    // Get batch

    default @NotNull List<@Nullable V> getAll(@NotNull K @NotNull [] keys) {
        return Arrays.stream(keys).map(this::get).toList();
    }

    default @NotNull List<@Nullable V> getAll(@NotNull Iterable<K> keys) {
        return Streams.stream(keys).map(this::get).toList();
    }

    // Contains

    default boolean containsKey(@NotNull K key) {
        return get(key) != null;
    }

    // Scanning

    boolean containsValue(@NotNull V value);

    default void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        for (Map.Entry<K, V> entry : entrySet()) {
            action.accept(entry.getKey(), entry.getValue());
        }
    }

    default @NotNull Iterable<K> keys() {
        return keySet();
    }

    @NotNull Set<K> keySet();

    @NotNull Collection<V> values();

    @NotNull Set<Map.Entry<K, V>> entrySet();

    default @NotNull Map<K, V> asMap() {
        return copyToMap();
    }

    default @NotNull Map<K, V> copyToMap() {
        return entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // Set/put single

    void set(@NotNull K key, @NotNull V value);

    default @Nullable V put(@NotNull K key, @NotNull V value) {
        V existing = get(key);
        set(key, value);
        return existing;
    }

    default @Nullable V putIfAbsent(@NotNull K key, @NotNull V value) {
        V existing = get(key);
        if (existing == null) {
            set(key, value);
        }
        return existing;
    }

    default @Nullable V putIfPresent(@NotNull K key, @NotNull V value) {
        return replace(key, value);
    }

    // Put batch

    default void putAll(@NotNull Map<? extends K, ? extends V> map) {
        map.forEach(this::set);
    }

    default void putAll(@NotNull Iterable<Map.Entry<? extends K, ? extends V>> entries) {
        entries.forEach(entry -> set(entry.getKey(), entry.getValue()));
    }

    default void putAll(@NotNull Stream<Map.Entry<? extends K, ? extends V>> entries) {
        entries.forEach(entry -> set(entry.getKey(), entry.getValue()));
    }

    default void putAll(@NotNull K @NotNull [] keys, @NotNull V @NotNull [] values) {
        assert keys.length == values.length : "Illegal arrays length: %d vs %d".formatted(keys.length, values.length);
        for (int i = 0, n = keys.length; i < n; i++) {
            set(keys[i], values[i]);
        }
    }

    default void putAll(@NotNull Iterable<? extends K> keys, @NotNull Iterable<? extends V> values) {
        BiStream.zip(Streams.stream(keys), Streams.stream(values)).forEach(this::set);
    }

    // Replace

    default @Nullable V replace(@NotNull K key, @NotNull V value) {
        V curValue;
        if (((curValue = get(key)) != null) || containsKey(key)) {
            curValue = put(key, value);
        }
        return curValue;
    }

    default boolean replace(@NotNull K key, @Nullable V oldValue, @NotNull V newValue) {
        Object curValue = get(key);
        if (!Objects.equals(curValue, oldValue) || (curValue == null && !containsKey(key))) {
            return false;
        }
        put(key, newValue);
        return true;
    }

    // Delete/remove single

    void delete(@NotNull K key);

    default @Nullable V remove(@NotNull K key) {
        V existing = get(key);
        delete(key);
        return existing;
    }

    // Remove batch

    default void removeAll(@NotNull K @NotNull [] keys) {
        Arrays.stream(keys).forEach(this::delete);
    }

    default void removeAll(@NotNull Iterable<K> keys) {
        keys.forEach(this::delete);
    }

    void clear();

    // I/O

    void flush();

    default void forceFlush() {
        flush();
    }

    void close();
}
