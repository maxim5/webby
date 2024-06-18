package io.spbx.webby.db.event;

import io.spbx.webby.db.managed.ManagedPersistent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface EventLog<E> extends ManagedPersistent {
    void append(@NotNull E event);

    void forEach(@NotNull Consumer<E> consumer);
}
