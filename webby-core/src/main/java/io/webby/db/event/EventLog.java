package io.webby.db.event;

import io.webby.db.cache.Persistable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface EventLog<E> extends Persistable {
    void append(@NotNull E event);

    void forEach(@NotNull Consumer<E> consumer);
}
