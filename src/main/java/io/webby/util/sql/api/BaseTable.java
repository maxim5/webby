package io.webby.util.sql.api;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BaseTable<E> extends Iterable<E> {
    @NotNull BaseTable<E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    int count();

    default boolean isEmpty() {
        return count() == 0;
    }

    @Override
    @NotNull ResultSetIterator<E> iterator();

    default @NotNull List<E> fetchAll() {
        try (ResultSetIterator<E> iterator = iterator()) {
            return Lists.newArrayList(iterator);
        }
    }

    @CanIgnoreReturnValue
    int insert(@NotNull E item);

    @NotNull TableMeta meta();
}
