package io.webby.util.collect;

import com.google.common.collect.Streams;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.webby.util.base.EasyCast.castAny;

public class EasyIterables {
    public static <E> @NotNull List<E> asList(@NotNull Iterable<? extends E> items) {
        return castAny(
                items instanceof List<?> list
                    ? list
                    : items instanceof Collection<?> collection
                        ? new ArrayList<>(collection)
                        : StreamSupport.stream(items.spliterator(), false).toList()
        );
    }

    public static <E> @NotNull List<E> concat(@NotNull Iterable<E> first, @NotNull Iterable<E> second) {
        return Stream.concat(Streams.stream(first), Streams.stream(second)).toList();
    }

    public static <E> @NotNull List<E> concat(@NotNull List<E> first, @NotNull List<E> second) {
        ArrayList<E> result = new ArrayList<>(first.size() + second.size());
        result.addAll(first);
        result.addAll(second);
        return result;
    }

    public static <E> @NotNull List<E> concatOne(@NotNull Iterable<E> first, @NotNull E second) {
        return Stream.concat(Streams.stream(first), Stream.of(second)).toList();
    }

    public static <E> long estimateSize(@NotNull Iterable<E> items, int def) {
        return items instanceof Collection<?> collection
                ? collection.size()
                : Math.min(items.spliterator().estimateSize(), def);
    }

    public static <E> int estimateSizeInt(@NotNull Iterable<E> items, int def) {
        return (int) estimateSize(items, def);
    }

    public static <E> @NotNull Optional<E> getOnlyItem(@NotNull Stream<E> stream) {
        List<E> all = stream.toList();
        return all.size() == 1 ? Optional.of(all.get(0)) : Optional.empty();
    }
}
