package io.webby.orm.api;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.MustBeClosed;
import io.webby.orm.api.entity.BatchEntityData;
import io.webby.orm.api.entity.EntityData;
import io.webby.orm.api.query.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface BaseTable<E> extends Iterable<E> {
    // Base

    @NotNull Engine engine();

    @NotNull QueryRunner runner();

    @NotNull TableMeta meta();

    @NotNull BaseTable<E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    // Size

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

    // Iteration

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

    default @Nullable E getFirstMatchingOrNull(@NotNull Filter filter) {
        try (ResultSetIterator<E> iterator = iterator(filter)) {
            return iterator.hasNext() ? iterator.next() : null;
        }
    }

    // INSERT

    @CanIgnoreReturnValue
    int insert(@NotNull E entity);

    @CanIgnoreReturnValue
    int insertIgnore(@NotNull E entity);

    @CanIgnoreReturnValue
    int insertData(@NotNull EntityData<?> data);

    @CanIgnoreReturnValue
    int[] insertBatch(@NotNull Collection<? extends E> batch);

    @CanIgnoreReturnValue
    int[] insertDataBatch(@NotNull BatchEntityData<?> batchData);

    // UPDATE

    @CanIgnoreReturnValue
    int updateDataWhere(@NotNull EntityData<?> data, @NotNull Where where);

    @CanIgnoreReturnValue
    int updateWhere(@NotNull E entity, @NotNull Where where);

    @CanIgnoreReturnValue
    int[] updateWhereBatch(@NotNull Collection<? extends E> batch, @NotNull Contextual<Where, E> where);

    @CanIgnoreReturnValue
    <B> int[] updateDataWhereBatch(@NotNull BatchEntityData<B> batchData, @NotNull Contextual<Where, B> where);

    // INSERT OR UPDATE

    @CanIgnoreReturnValue
    default int updateWhereOrInsert(@NotNull E entity, @NotNull Where where) {
        int updated = updateWhere(entity, where);
        if (updated == 0) {
            return insert(entity);
        }
        return updated;
    }

    @CanIgnoreReturnValue
    default int updateWhereOrInsertData(@NotNull EntityData<?> data, @NotNull Where where) {
        int updated = updateDataWhere(data, where);
        if (updated == 0) {
            return insertData(data);
        }
        return updated;
    }

    // DELETE

    @CanIgnoreReturnValue
    int deleteWhere(@NotNull Where where);
}
