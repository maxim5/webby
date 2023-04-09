package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link Foreign} implementation for integer FK.
 */
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

    @Override
    public boolean equals(Object o) {
        return o instanceof ForeignInt<?> that && id == that.id && Objects.equals(entity.get(), that.entity.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, entity.get());
    }

    @Override
    public String toString() {
        return "ForeignInt{id=%d, entity=%s}".formatted(id, entity);
    }
}
