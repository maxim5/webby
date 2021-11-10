package io.webby.util.sql.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public final class ForeignLong<E> implements Foreign<Long, E> {
    private final long id;
    private final AtomicReference<E> entity;

    public ForeignLong(long id, @Nullable E entity) {
        this.id = id;
        this.entity = new AtomicReference<>(entity);
    }

    public static <T> @NotNull ForeignLong<T> ofId(long id) {
        return new ForeignLong<>(id, null);
    }

    public static <T> @NotNull ForeignLong<T> ofEntity(long id, @NotNull T entity) {
        return new ForeignLong<>(id, entity);
    }

    public long getLongId() {
        return id;
    }

    @Override
    public @NotNull Long getFk() {
        return getLongId();
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
