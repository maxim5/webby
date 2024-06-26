package io.spbx.util.func;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Represents a specialized {@link Function} which distinguishes nullable and non-null input and output.
 * <p>
 * The client may call {@link #apply(Object)} just like a usual {@link Function}. This method allows nulls
 * for both {@code U} and {@code V}. Or may use a non-null version {@link #applyNotNull(Object)}.
 * <p>
 * Different implementations may
 * <ul>
 *     <li>do not allow null values and throw</li>
 *     <li>allow nullable values both ways and exclusively map null to null</li>
 *     <li>allow nullable values only in {@code U} or only in {@code V} space</li>
 *     <li>mix and match nulls and non-nulls in some other way</li>
 * </ul>
 *
 * @param <U> argument type
 * @param <V> result type
 */
@FunctionalInterface
public interface NullAwareFunction<U, V> extends Function<U, V> {
    /**
     * Applies a non-null version of this function.
     *
     * @param u the (non-null) function argument
     * @return the (non-null) function result
     */
    @NotNull V applyNotNull(@NotNull U u);

    /**
     * Applies a null-allowable version of this function. By default, exclusively maps null to null.
     *
     * @param u the (nullable) function argument
     * @return the (nullable) function result
     */
    @Override
    default @Nullable V apply(@Nullable U u) {
        return u != null ? applyNotNull(u) : null;
    }

    /**
     * Converts a simple non-null {@link Function} into a {@link NullAwareFunction}.
     * A wrapped function would be safely accept nullable inputs and pass them through.
     * Note that this means the output of {@link #apply(Object)} is becoming nullable too.
     *
     * @param function the non-null function to wrap
     * @return the null-aware function
     */
    static <U, V> @NotNull NullAwareFunction<U, V> wrap(@NotNull Function<U, V> function) {
        return function::apply;
    }
}
