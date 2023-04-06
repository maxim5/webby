package io.webby.util.collect;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collector;
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

    public static <E> long estimateSize(@NotNull Iterable<E> items, int def) {
        return items instanceof Collection<?> collection
                ? collection.size()
                : Math.min(items.spliterator().estimateSize(), def);
    }

    public static <E> int estimateSizeInt(@NotNull Iterable<E> items, int def) {
        return (int) estimateSize(items, def);
    }

    private static final Object MORE_THAN_ONE_ITEM = new Object();
    private static final Collector<Object, ?, Optional<Object>> ONLY_ITEM_OR_EMPTY = Collector.of(
        AtomicReference::new,
        (ref, obj) -> ref.updateAndGet(cur -> cur != null ? MORE_THAN_ONE_ITEM : obj),
        (ref1, ref2) -> {
            ref1.compareAndSet(null, ref2.get());
            return ref1;
        },
        ref -> Optional.ofNullable(ref.get() == MORE_THAN_ONE_ITEM ? null : ref.get())
    );

    public static <E> @NotNull Collector<E, ?, Optional<E>> getOnlyItemOrEmpty() {
        return castAny(ONLY_ITEM_OR_EMPTY);
    }
}
