package io.webby.db.event;

import io.webby.db.managed.ManagedPersistent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface EventLog<E> extends ManagedPersistent {
    void append(@NotNull E event);

    void forEach(@NotNull Consumer<E> consumer);
}
