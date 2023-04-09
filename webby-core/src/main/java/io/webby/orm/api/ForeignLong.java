package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link Foreign} implementation for long FK.
 */
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

    @Override
    public boolean equals(Object o) {
        return o instanceof ForeignLong<?> that && id == that.id && Objects.equals(entity.get(), that.entity.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, entity.get());
    }

    @Override
    public String toString() {
        return "ForeignLong{id=%d, entity=%s}".formatted(id, entity);
    }
}
