package io.webby.util.collect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class OneOf<U, V> {
    private final U first;
    private final V second;
    private final Which which;

    private OneOf(U first, V second, Which which) {
        this.first = first;
        this.second = second;
        this.which = which;
    }

    public static <U, V> OneOf<U, V> of(U first, V second) {
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

    public <T> @NotNull OneOf<T, V> mapFirst(@NotNull Function<U, T> convert) {
        return fromEither(first -> ofFirst(convert.apply(first)), OneOf::ofSecond);
    }

    public <T> @NotNull OneOf<U, T> mapSecond(@NotNull Function<V, T> convert) {
        return fromEither(OneOf::ofFirst, second -> ofSecond(convert.apply(second)));
    }

    public <T> @NotNull T fromEither(@NotNull Function<U, T> fromFirst, @NotNull Function<V, T> fromSecond) {
        return hasFirst() ? fromFirst.apply(first) : fromSecond.apply(second);
    }

    @Override
    public String toString() {
        return hasFirst() ? "OneOf{first=%s}".formatted(first) : "OneOf{second=%s}".formatted(second);
    }

    public enum Which {
        FIRST,
        SECOND
    }
}
