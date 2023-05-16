package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static io.webby.util.base.EasyCast.castAny;

/**
 * A {@link Foreign} implementation for integer FK. Note: zero ids are treated as <code>NULL</code>s.
 */
public final class ForeignInt<E> implements Foreign<Integer, E> {
    private static final ForeignInt<?> EMPTY = new ForeignInt<>(0, null);

    private final int id;
    private final AtomicReference<E> entity;

    public ForeignInt(int id, @Nullable E entity) {
        assert id != 0 || entity == null : "Invalid foreign: id=%s entity=%s".formatted(id, entity);
        this.id = id;
        this.entity = new AtomicReference<>(entity);
    }

    public static <T> @NotNull ForeignInt<T> empty() {
        return castAny(EMPTY);
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
    public @Nullable Integer getFk() {
        return id == 0 ? null : id;
    }

    @Override
    public @NotNull Integer getFkOrDie() {
        assert id != 0 : "The foreign is empty: " + this;
        return id;
    }

    @Override
    public @Nullable E getEntity() {
        return entity.get();
    }

    @Override
    public boolean isEmpty() {
        return id == 0;
    }

    @Override
    public boolean isPresent() {
        return id != 0;
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

    public static <E> boolean isMatch(@NotNull ForeignInt<E> left, @NotNull ForeignInt<E> right) {
        return left.id == right.id && Foreign.isEntityMatch(left, right);
    }
}
