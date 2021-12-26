package io.webby.db.event;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.Flushable;
import java.util.List;

public interface KeyEventStore<K, E> extends Flushable, Closeable {
    void append(@NotNull K key, @NotNull E event);

    @NotNull List<E> getAll(@NotNull K key);

    void deleteAll(@NotNull K key);

    @Override
    void flush();

    void forceFlush();

    @Override
    void close();
}
