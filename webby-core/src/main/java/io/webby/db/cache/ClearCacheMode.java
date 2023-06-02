package io.webby.db.cache;

/**
 * Represents the preference for the cache clear operation.
 */
public enum ClearCacheMode {
    /**
     * Means that complete cache purge is expected.
     * The internal cache used by the storage systems must be clean.
     */
    FORCE_CLEAR_ALL,
    /**
     * Means that complete cache purge is not expected.
     * The storage systems may reduce the cache to the soft limit size, or do nothing.
     */
    COMPACT_IF_NECESSARY,
}
