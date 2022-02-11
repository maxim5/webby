package io.webby.db.event;

import io.webby.db.cache.Persistable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface KeyEventStore<K, E> extends Persistable {
    void append(@NotNull K key, @NotNull E event);

    @NotNull List<E> getAll(@NotNull K key);

    void deleteAll(@NotNull K key);
}
