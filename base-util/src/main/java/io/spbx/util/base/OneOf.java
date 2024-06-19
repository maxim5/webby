package io.spbx.util.base;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.*;

/**
 * Can hold an immutable pair of nullable objects, only one of which is set at a time.
 */
@Immutable
public class OneOf<U, V> {
    private final U first;
    private final V second;
    private final Which which;

    private OneOf(U first, V second, Which which) {
        this.first = first;
        this.second = second;
        this.which = which;
    }

    public static <U, V> @NotNull OneOf<U, V> of(U first, V second) {
        assert first == null ^ second == null : "Exactly one must be non-null: first=%s second=%s".formatted(first, second);
        if (first != null) {
            return ofFirst(first);
        } else {
            return ofSecond(second);
        }
    }

    public static <U, V> OneOf<U, V> ofFirst(U first) {
        assert first != null : "Expected non-null `first`";
        return new OneOf<>(first, null, Which.FIRST);
    }

    public static <U, V> OneOf<U, V> ofSecond(V second) {
        assert second != null : "Expected non-null `second`";
        return new OneOf<>(null, second, Which.SECOND);
    }

    public @NotNull Which getCase() {
        return which;
    }

    public boolean hasFirst() {
        return which == Which.FIRST;
    }

    public @Nullable U first() {
        return first;
    }

    public boolean hasSecond() {
        return which == Which.SECOND;
    }

    public @Nullable V second() {
        return second;
    }

    public <T, S> @NotNull OneOf<T, S> map(@NotNull Function<U, T> convertFirst, @NotNull Function<V, S> convertSecond) {
        return mapToObj(first -> ofFirst(convertFirst.apply(first)), second -> ofSecond(convertSecond.apply(second)));
    }

    public <T> @NotNull OneOf<T, V> mapFirst(@NotNull Function<U, T> convert) {
        assert hasFirst() : "Can't mapFirst() because first is not set: " + this;
        return map(convert, second -> second);
    }

    public <T> @NotNull OneOf<U, T> mapSecond(@NotNull Function<V, T> convert) {
        assert hasSecond() : "Can't mapSecond() because second is not set: " + this;
        return map(first -> first, convert);
    }

    public <T> @NotNull T mapToObj(@NotNull Function<U, T> fromFirst, @NotNull Function<V, T> fromSecond) {
        return hasFirst() ? fromFirst.apply(first) : fromSecond.apply(second);
    }

    public int mapToInt(@NotNull ToIntFunction<U> fromFirst, @NotNull ToIntFunction<V> fromSecond) {
        return hasFirst() ? fromFirst.applyAsInt(first) : fromSecond.applyAsInt(second);
    }

    public long mapToLong(@NotNull ToLongFunction<U> fromFirst, @NotNull ToLongFunction<V> fromSecond) {
        return hasFirst() ? fromFirst.applyAsLong(first) : fromSecond.applyAsLong(second);
    }

    public double mapToDouble(@NotNull ToDoubleFunction<U> fromFirst, @NotNull ToDoubleFunction<V> fromSecond) {
        return hasFirst() ? fromFirst.applyAsDouble(first) : fromSecond.applyAsDouble(second);
    }

    public boolean testFirstIfSet(@NotNull Predicate<U> predicate) {
        return hasFirst() && predicate.test(first);
    }

    public boolean testSecondIfSet(@NotNull Predicate<V> predicate) {
        return hasSecond() && predicate.test(second);
    }

    public boolean test(@NotNull Predicate<U> forFirst, @NotNull Predicate<V> forSecond) {
        return hasFirst() ? forFirst.test(first) : forSecond.test(second);
    }

    public void apply(@NotNull Consumer<U> takeFirst, @NotNull Consumer<V> takeSecond) {
        if (hasFirst()) {
            takeFirst.accept(first);
        } else {
            takeSecond.accept(second);
        }
    }

    @Override
    public String toString() {
        return hasFirst() ? "OneOf{first=%s}".formatted(first) : "OneOf{second=%s}".formatted(second);
    }

    public enum Which {
        FIRST,
        SECOND,
    }
}
