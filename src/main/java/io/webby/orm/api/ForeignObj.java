package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class ForeignObj<I, E> implements Foreign<I, E> {
    private final I id;
    private final AtomicReference<E> entity;

    public ForeignObj(@NotNull I id, @Nullable E entity) {
        this.id = id;
        this.entity = new AtomicReference<>(entity);
    }

    public static <K, V> @NotNull ForeignObj<K, V> ofId(@NotNull K id) {
        return new ForeignObj<>(id, null);
    }

    public static <K, V> @NotNull ForeignObj<K, V> ofEntity(@NotNull K id, @NotNull V entity) {
        return new ForeignObj<>(id, entity);
    }

    @Override
    public @NotNull I getFk() {
        return id;
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
        return o instanceof ForeignObj<?, ?> that && id.equals(that.id) && Objects.equals(entity.get(), that.entity.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, entity.get());
    }

    @Override
    public String toString() {
        return "ForeignObj{id=%s, entity=%s}".formatted(id, entity);
    }
}
