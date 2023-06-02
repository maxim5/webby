package io.webby.db.managed;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.Flushable;
import java.io.UncheckedIOException;

/**
 * Represents the persistent storage managed by the system.
 * <p>
 * The API provides flush and cache clear operations that can be called manually
 * or by the storage itself (in case the internal cache hits the hard limit), but
 * in addition registers the storage for automatic management by the background threads.
 * A forced flush may happen due to a server event (maintenance mode or termination),
 * resource shortage or just periodically.
 */
public interface ManagedPersistent extends Closeable, Flushable {
    /**
     * Flushes the possibly cached data to persistent storage with the {@code FlushMode.INCREMENTAL} preference.
     */
    @Override
    default void flush() throws UncheckedIOException {
        flush(FlushMode.INCREMENTAL);
    }

    /**
     * Flushes the possibly cached data to persistent storage with the {@code mode} preference.
     */
    void flush(@NotNull FlushMode mode) throws UncheckedIOException;

    /**
     * Closes the storage. Forces the {@code FlushMode.FULL_CLEAR} cache flush.
     */
    default void close() throws UncheckedIOException {
        flush(FlushMode.FULL_CLEAR);
    }
}
