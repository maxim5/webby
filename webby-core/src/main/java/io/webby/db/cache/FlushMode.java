package io.webby.db.cache;

import org.jetbrains.annotations.NotNull;

public enum FlushMode {
    INCREMENTAL(ClearCacheMode.COMPACT_IF_NECESSARY),
    FULL_CLEAR(ClearCacheMode.FORCE_CLEAR_ALL),
    FULL_COMPACT(ClearCacheMode.COMPACT_IF_NECESSARY);

    private final ClearCacheMode clearCacheMode;

    FlushMode(@NotNull ClearCacheMode clearCacheMode) {
        this.clearCacheMode = clearCacheMode;
    }

    public @NotNull ClearCacheMode clearCacheMode() {
        return clearCacheMode;
    }

    public boolean isFlushAll() {
        return this != INCREMENTAL;
    }
}
