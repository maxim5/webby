package io.webby.db.cache;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.UncheckedIOException;

public interface Persistable extends Closeable, Flushable {
    @Override
    default void flush() throws UncheckedIOException {
        flush(FlushMode.INCREMENTAL);
    }

    void flush(@NotNull FlushMode mode) throws UncheckedIOException;

    default void close() throws UncheckedIOException {
        flush(FlushMode.FULL_CLEAR);
    }
}
