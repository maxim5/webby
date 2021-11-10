package io.webby.util.sql.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public final class ForeignInt<E> implements Foreign<Integer, E> {
    private final int id;
    private final AtomicReference<E> entity;

    public ForeignInt(int id, @Nullable E entity) {
        this.id = id;
        this.entity = new AtomicReference<>(entity);
    }

    public static <T> @NotNull ForeignInt<T> ofId(int id) {
        return new ForeignInt<>(id, null);
    }

    public static <T> @NotNull ForeignInt<T> ofEntity(int id, @NotNull T entity) {
        return new ForeignInt<>(id, entity);
    }

    public int getIntId() {
        return id;
    }

    @Override
    public @NotNull Integer getFk() {
        return getIntId();
    }

    @Override
    public @Nullable E getEntity() {
        return entity.get();
    }

    @Override
    public boolean setEntityIfMissing(@NotNull E entity) {
        return this.entity.compareAndSet(null, entity);
    }

    @Override
    public void setEntityUnconditionally(@NotNull E entity) {
        this.entity.set(entity);
    }
}
