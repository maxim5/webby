package io.webby.db.event;

import org.jetbrains.annotations.NotNull;

public record EventStoreOptions<K, E>(@NotNull String name,
                                      @NotNull Class<K> key,
                                      @NotNull Class<E> value,
                                      @NotNull KeyEventStoreFactory.Compacter<E> compacter) {
    public static <K, V> @NotNull EventStoreOptions<K, V> of(@NotNull String name,
                                                             @NotNull Class<K> key,
                                                             @NotNull Class<V> value) {
        return new EventStoreOptions<>(name, key, value, list -> list);
    }
}
