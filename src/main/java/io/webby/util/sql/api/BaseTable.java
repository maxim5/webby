package io.webby.util.sql.api;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.util.sql.api.query.Where;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BaseTable<E> extends Iterable<E> {
    @NotNull BaseTable<E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    @NotNull Engine engine();

    int count();

    int count(@NotNull Where where);

    default boolean isEmpty() {
        return count() == 0;
    }

    @Override
    @NotNull ResultSetIterator<E> iterator();

    @NotNull ResultSetIterator<E> iterator(@NotNull Where where);

    default @NotNull List<E> fetchAll() {
        try (ResultSetIterator<E> iterator = iterator()) {
            return Lists.newArrayList(iterator);
        }
    }

    default @NotNull List<E> fetchMatching(@NotNull Where where) {
        try (ResultSetIterator<E> iterator = iterator(where)) {
            return Lists.newArrayList(iterator);
        }
    }

    @CanIgnoreReturnValue
    int insert(@NotNull E item);

    @NotNull TableMeta meta();
}
