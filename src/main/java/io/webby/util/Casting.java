package io.webby.util;

import java.util.Map;

public interface Casting {
    @SuppressWarnings("unchecked")
    static <K, V> Map<K, V> castMap(Map<?, ?> map) {
        return (Map<K, V>) map;
    }

    @SuppressWarnings("unchecked")
    static <R, T> R castAny(T object) {
        return (R) object;
    }
}
