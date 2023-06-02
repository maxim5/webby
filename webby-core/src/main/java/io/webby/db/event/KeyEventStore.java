package io.webby.db.event;

import io.webby.db.managed.ManagedPersistent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Allows to store events grouped by key.
 * Essentially, {@link KeyEventStore} is an append-only form of {@code K -> List<E>} map.
 *
 * @param <K> the key type
 * @param <E> event type (must have a codec)
 */
public interface KeyEventStore<K, E> extends ManagedPersistent {
    /**
     * Appends the {@code event} by the {@code key}.
     */
    void append(@NotNull K key, @NotNull E event);

    /**
     * Returns the list of events for the given {@code key}.
     */
    @NotNull List<E> getAll(@NotNull K key);

    /**
     * Drops all events per the {@code key}.
     */
    void deleteAll(@NotNull K key);
}
