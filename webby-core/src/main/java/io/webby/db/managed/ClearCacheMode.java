package io.webby.db.managed;

/**
 * Represents the preference for the cache clear operation.
 */
public enum ClearCacheMode {
    /**
     * Means that complete cache purge is expected.
     * All internal caches used by the storage systems must be clean, i.e. everything is persisted.
     */
    FORCE_CLEAR_ALL,
    /**
     * Means that complete cache purge is not expected.
     * The storage systems may reduce the cache to the soft limit size, or do nothing.
     */
    COMPACT_IF_NECESSARY,
}
