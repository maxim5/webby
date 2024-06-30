package io.spbx.util.collect;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClassMap<T> extends AbstractMap<Class<?>, T> implements Map<Class<?>, T> {
    private final Map<Class<?>, T> map;

    public ClassMap(@NotNull Map<Class<?>, T> map) {
        this.map = map;
    }

    public static <T> @NotNull ClassMap<T> mutable() {
        return new ClassMap<>(new HashMap<>());
    }

    public static <T> @NotNull ClassMap<T> ordered() {
        return new ClassMap<>(new LinkedHashMap<>());
    }

    public static <T> @NotNull ClassMap<T> concurrent() {
        return new ClassMap<>(new ConcurrentHashMap<>());
    }

    public static <T> @NotNull ClassMap<T> immutableOf(@NotNull Map<Class<?>, T> map) {
        return new ClassMap<>(ImmutableMap.copyOf(map));
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public @Nullable T get(Object key) {
        return map.get(key);
    }

    public @Nullable T getClass(@NotNull Class<?> key) {
        return map.get(key);
    }

    public @Nullable T getSuper(@NotNull Class<?> key) {
        return getClassOrSuper(key);
    }

    // https://stackoverflow.com/questions/32229528/inheritance-aware-class-to-value-map-to-replace-series-of-instanceof
    private @Nullable T getClassOrSuper(@Nullable Class<?> key) {
        T value = key != null ? map.get(key) : null;
        return value == null && key != null ? getClassOrSuper(key.getSuperclass()) : value;
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @CanIgnoreReturnValue
    public T put(Class<?> key, T value) {
        return map.put(key, value);
    }

    @CanIgnoreReturnValue
    public <V extends T> T putInstance(V value) {
        return map.put(value.getClass(), value);
    }

    @Override
    public @NotNull Set<Entry<Class<?>, T>> entrySet() {
        return map.entrySet();
    }
}
