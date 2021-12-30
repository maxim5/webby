package io.webby.orm.api;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.MustBeClosed;
import io.webby.orm.api.query.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public interface BaseTable<E> extends Iterable<E> {
    @NotNull Engine engine();

    @NotNull QueryRunner runner();

    @NotNull BaseTable<E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    int count();

    int count(@NotNull Filter filter);

    default boolean isEmpty() {
        return !isNotEmpty();
    }

    default boolean isNotEmpty() {
        return count() > 0;
    }

    default boolean exists(@NotNull Where where) {
        return count(where) > 0;
    }

    @Override
    @MustBeClosed
    @NotNull ResultSetIterator<E> iterator();

    @MustBeClosed
    @NotNull ResultSetIterator<E> iterator(@NotNull Filter filter);

    @Override
    default void forEach(@NotNull Consumer<? super E> action) {
        try (ResultSetIterator<E> iterator = iterator()) {
            iterator.forEachRemaining(action);
        }
    }

    default void forEach(@NotNull Filter filter, @NotNull Consumer<? super E> action) {
        try (ResultSetIterator<E> iterator = iterator(filter)) {
            iterator.forEachRemaining(action);
        }
    }

    default @NotNull List<E> fetchAll() {
        try (ResultSetIterator<E> iterator = iterator()) {
            return Lists.newArrayList(iterator);
        }
    }

    default @NotNull List<E> fetchMatching(@NotNull Filter filter) {
        try (ResultSetIterator<E> iterator = iterator(filter)) {
            return Lists.newArrayList(iterator);
        }
    }

    default @NotNull Page<E> fetchPage(@NotNull CompositeFilter clause) {
        List<E> items = fetchMatching(clause);
        Offset offset = clause.offset();
        LimitClause limit = clause.limit();
        boolean isFullPage = limit != null && items.size() == limit.limitValue();
        if (isFullPage) {
            PageToken pageToken = new PageToken(null, (offset != null ? offset.offsetValue() : 0) + limit.limitValue());
            return new Page<>(items, pageToken);
        }
        return new Page<>(items, null);
    }

    @CanIgnoreReturnValue
    int insert(@NotNull E entity);

    @CanIgnoreReturnValue
    int updateWhere(@NotNull E entity, @NotNull Where where);

    @CanIgnoreReturnValue
    int deleteWhere(@NotNull Where where);

    @NotNull TableMeta meta();
}