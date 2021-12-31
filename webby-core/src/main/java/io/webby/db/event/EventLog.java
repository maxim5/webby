package io.webby.db.event;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface EventLog<E> extends Persistable {
    void append(@NotNull E event);

    void forEach(@NotNull Consumer<E> consumer);
}
