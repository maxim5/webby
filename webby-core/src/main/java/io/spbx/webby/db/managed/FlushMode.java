package io.spbx.webby.db.managed;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the preference for the flush operation.
 */
public enum FlushMode {
    /**
     * Means that complete cache flush and purge are not expected (but may be done).
     * The storage systems may flush and reduce the cache to the soft limit size, or do nothing.
     */
    INCREMENTAL(ClearCacheMode.COMPACT_IF_NECESSARY),
    /**
     * Means that complete cache flush and purge is expected.
     * The internal cache used by the storage systems must be clean and all data persisted.
     */
    FULL_CLEAR(ClearCacheMode.FORCE_CLEAR_ALL),
    /**
     * Means that complete cache flush but not purge is expected.
     * The storage systems may flush and reduce the cache to the soft limit size, or do nothing,
     * but all data must be persisted.
     */
    FULL_COMPACT(ClearCacheMode.COMPACT_IF_NECESSARY);

    private final ClearCacheMode clearCacheMode;

    FlushMode(@NotNull ClearCacheMode clearCacheMode) {
        this.clearCacheMode = clearCacheMode;
    }

    public @NotNull ClearCacheMode clearCacheMode() {
        return clearCacheMode;
    }

    /**
     * Returns whether all data must be flushed (to make all in-memory data persisted).
     */
    public boolean isFlushAll() {
        return this != INCREMENTAL;
    }
}
