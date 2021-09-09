package io.webby.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

import static io.webby.util.EasyCast.castAny;

public class EasyIterables {
    public static <E> List<E> asList(@NotNull Iterable<? extends E> items) {
        return castAny(
                items instanceof List<?> list
                    ? list
                    : items instanceof Collection<?> collection
                        ? new ArrayList<>(collection)
                        : StreamSupport.stream(items.spliterator(), false).toList()
        );
    }

    public static <E> long estimateSize(@NotNull Iterable<E> items, int def) {
        return items instanceof Collection<?> collection
                ? collection.size()
                : Math.min(items.spliterator().estimateSize(), def);
    }

    public static <E> int estimateSizeInt(@NotNull Iterable<E> items, int def) {
        return (int) estimateSize(items, def);
    }
}
