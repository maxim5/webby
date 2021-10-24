package io.webby.util.sql.api;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BaseTable<E> extends Iterable<E> {
    int count();

    default boolean isEmpty() {
        return count() == 0;
    }

    @Override
    ResultSetIterator<E> iterator();

    default @NotNull List<E> fetchAll() {
        try (ResultSetIterator<E> iterator = iterator()) {
            return Lists.newArrayList(iterator);
        }
    }

    int insert(@NotNull E item);
}
