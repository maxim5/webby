package io.spbx.webby.db.managed;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the API to return the underlying cache used by the storage system.
 * The storage may or may not be managed, just has to have a cache.
 * @param <C> cache type
 * @see ManagedPersistent
 */
public interface HasCache<C> {
    /**
     * Returns the cache used by the storage system.
     * Result may be the copy of the current cache or a live instance.
     */
    @NotNull C cache();
}
