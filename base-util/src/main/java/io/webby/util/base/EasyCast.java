package io.webby.util.base;

import java.util.Map;

public class EasyCast {
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> castMap(Map<?, ?> map) {
        return (Map<K, V>) map;
    }

    @SuppressWarnings("unchecked")
    public static <R, T> R castAny(T object) {
        return (R) object;
    }
}
