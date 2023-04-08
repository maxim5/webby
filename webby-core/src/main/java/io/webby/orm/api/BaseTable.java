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

public interface BaseTable<E> extends Iterable<E>, HasEngine, HasRunner {
    // Base

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

    /**
     * Inserts the {@code entity} into the table.
     * @return the number of affected rows in a table (1 if successful)
     * @throws QueryException if the insertion failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    int insert(@NotNull E entity);

    /**
     * Inserts the {@code entity} into the table, ignoring PK or FK conflicts.
     * @return the number of affected rows in a table (1 if successful, 0 otherwise)
     * @throws QueryException if the insertion failed unexpectedly
     */
    @CanIgnoreReturnValue
    int insertIgnore(@NotNull E entity);

    /**
     * Inserts the {@code EntityData} into the table. The {@code data} row may or may not be complete.
     * @return the number of affected rows in a table (1 if successful)
     * @throws QueryException if the insertion failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    int insertData(@NotNull EntityData<?> data);

    /**
     * Inserts multiple entities into the table in batch mode.
     * @return the array holding the number of affected rows in a table (1 if successful)
     * @throws QueryException if the insertion failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    int[] insertBatch(@NotNull Collection<? extends E> batch);

    /**
     * Inserts the {@code BatchEntityData} into the table. The {@code batchData} rows may or may not be complete.
     * @return the array holding the number of affected rows in a table (1 if successful)
     * @throws QueryException if the insertion failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    int[] insertDataBatch(@NotNull BatchEntityData<?> batchData);

    // UPDATE

    /**
     * Updates the {@code entity} in the table given the {@code where} condition.
     * @return the number of affected rows in a table (1 or more if successful)
     * @throws QueryException if the update failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    int updateWhere(@NotNull E entity, @NotNull Where where);

    /**
     * Updates the {@code EntityData} in the table given the {@code where} condition.
     * @return the number of affected rows in a table (1 or more if successful)
     * @throws QueryException if the update failed, e.g. due to PK or FK conflict
     */
    @CanIgnoreReturnValue
    int updateDataWhere(@NotNull EntityData<?> data, @NotNull Where where);

    /**
     * Updates multiple rows in the table given the contextual {@code where} condition.
     * In order to pass correct query params, the condition gets resolved from each entity in the {@code batch}.
     * @return the array holding the number of affected rows in a table (1 if successful)
     * @throws QueryException if the update failed, e.g. due to PK or FK conflict
     * @see Contextual for arg resolution
     */
    @CanIgnoreReturnValue
    int[] updateWhereBatch(@NotNull Collection<? extends E> batch, @NotNull Contextual<Where, E> where);

    /**
     * Updates multiple rows in the table given the contextual {@code where} condition.
     * In order to pass correct query params, the condition gets resolved from each chunk in the {@code batchData}.
     * @return the array holding the number of affected rows in a table (1 if successful)
     * @throws QueryException if the update failed, e.g. due to PK or FK conflict
     * @param <B> the type of batch used for context resolution
     * @see Contextual for arg resolution
     */
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
