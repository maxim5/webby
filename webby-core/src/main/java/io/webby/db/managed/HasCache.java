package io.webby.db.managed;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the API to return the underlying cache used by the storage system.
 * @param <C> cache type
 */
public interface HasCache<C> {
    /**
     * Returns the cache used by the storage system.
     * Result may be the copy of the current cache or a live instance.
     */
    @NotNull C cache();
}
