package io.webby.util.base;

import java.util.Map;

public interface EasyCast {
    @SuppressWarnings("unchecked")
    static <K, V> Map<K, V> castMap(Map<?, ?> map) {
        return (Map<K, V>) map;
    }

    @SuppressWarnings("unchecked")
    static <R, T> R castAny(T object) {
        return (R) object;
    }
}
