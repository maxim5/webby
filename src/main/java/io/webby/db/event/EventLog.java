package io.webby.db.event;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.Flushable;

public interface EventLog<E> extends Flushable, Closeable {
    void append(@NotNull E event);

    @Override
    void flush();

    @Override
    void close();
}
