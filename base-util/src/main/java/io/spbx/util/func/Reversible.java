package io.spbx.util.func;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a reversible function, which can be applied both ways:
 * forward from {@code U} to {@code V} and backward from {@code V} to {@code U}.
 * <p>
 * The invariants which must be held by all implementations are the following:
 * <ul>
 *     <li>The {@link #forward(Object)} of the {@link #backward(Object)} (and vice versa) is the identity function.</li>
 *     <li>The {@link #forwardNullable(Object)} of the {@link #backwardNullable(Object)} (and vice versa)
 *     is the identity function.</li>
 *     <li>The {@link #reverse()} swaps the {@link #forward(Object)} and {@link #backward(Object)} transformations,
 *     which means that double {@link #reverse()} returns the original function.</li>
 * </ul>
 * <p>
 * The implementations can distinguish nullable and non-null transformations, as per {@link NullAwareFunction} contract.
 *
 * @see com.google.common.base.Converter
 */
public interface Reversible<U, V> extends NullAwareFunction<U, V> {
    /**
     * A non-null version of the converter from {@code U} to {@code V}.
     *
     * @param u the (non-null) function argument in {@code U} space
     * @return the (non-null) function result in {@code V} space
     */
    @NotNull V forward(@NotNull U u);

    /**
     * A non-null version of the converter from {@code V} to {@code U}.
     *
     * @param v the (non-null) function argument in {@code V} space
     * @return the (non-null) function result in {@code U} space
     */
    @NotNull U backward(@NotNull V v);

    /**
     * Returns the reversed function which converts from {@code V} to {@code U} forward and
     * from {@code U} to {@code V} backward.
     */
    default @NotNull Reversible<V, U> reverse() {
        return new Reversible<>() {
            @Override
            public @NotNull U forward(@NotNull V v) {
                return Reversible.this.backward(v);
            }

            @Override
            public @NotNull V backward(@NotNull U u) {
                return Reversible.this.forward(u);
            }

            @Override
            public @NotNull Reversible<U, V> reverse() {
                return Reversible.this;
            }
        };
    }

    /**
     * A nullable version of the converter from {@code U} to {@code V}. By default, exclusively maps null to null.
     *
     * @param u the (non-null) function argument in {@code U} space
     * @return the (non-null) function result in {@code V} space
     */
    default @Nullable V forwardNullable(@Nullable U u) {
        return u != null ? forward(u) : null;
    }

    /**
     * A nullable version of the converter from {@code V} to {@code U}. By default, exclusively maps null to null.
     *
     * @param v the (non-null) function argument in {@code V} space
     * @return the (non-null) function result in {@code U} space
     */
    default @Nullable U backwardNullable(@Nullable V v) {
        return v != null ? backward(v) : null;
    }

    @Override
    default @NotNull V applyNotNull(@NotNull U u) {
        return forward(u);
    }

    @Override
    default @Nullable V apply(@Nullable U u) {
        return forwardNullable(u);
    }
}
