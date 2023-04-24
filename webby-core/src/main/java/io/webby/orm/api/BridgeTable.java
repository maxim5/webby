package io.webby.orm.api;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.MustBeClosed;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a bridge table, i.e. holding a many-to-many association. The bridge table consists of two
 * foreign keys (and potentially other columns):
 * <ul>
 *     <li>the "left" foreign key indexed by {@code IL} and referencing {@code EL} entities</li>
 *     <li>the "right" foreign key indexed by {@code IR} and referencing {@code ER} entities</li>
 * </ul>
 *
 * @param <IL> the type of index of the left entity
 * @param <EL> the left entity type
 * @param <IR> the type of index of the right entity
 * @param <ER> the right entity type
 */
public interface BridgeTable<IL, EL, IR, ER> {
    /**
     * Returns whether the table contains a <code>(leftIndex, rightIndex)</code> entry.
     */
    boolean exists(@NotNull IL leftIndex, @NotNull IR rightIndex);

    /**
     * Returns whether the table contains any association of <code>leftIndex</code> with any right entity.
     */
    default boolean existsLeft(@NotNull IL leftIndex) {
        return countRights(leftIndex) > 0;
    }

    /**
     * Returns whether the table contains any association of <code>rightIndex</code> with any left entity.
     */
    default boolean existsRight(@NotNull IR rightIndex) {
        return countLefts(rightIndex) > 0;
    }

    /**
     * Returns the number of association entries containing the <code>leftIndex</code>.
     */
    int countLefts(@NotNull IR rightIndex);

    /**
     * Returns the number of association entries containing the <code>rightIndex</code>.
     */
    int countRights(@NotNull IL leftIndex);

    /**
     * Returns an iterator over the left entities associated with the <code>rightIndex</code>.
     * <b>Important</b>: the caller is responsible for closing the iterator:
     * <pre>
     *     try (ResultSetIterator&lt;Entity&gt; iterator = table.iterateLefts(index)) {
     *         iterator.forEachRemaining(action);
     *     }
     * </pre>
     */
    @MustBeClosed
    @NotNull ResultSetIterator<EL> iterateLefts(@NotNull IR rightIndex);

    /**
     * Returns an iterator over the right entities associated with the <code>leftIndex</code>.
     * <b>Important</b>: the caller is responsible for closing the iterator:
     * <pre>
     *     try (ResultSetIterator&lt;Entity&gt; iterator = table.iterateRights(index)) {
     *         iterator.forEachRemaining(action);
     *     }
     * </pre>
     */
    @MustBeClosed
    @NotNull ResultSetIterator<ER> iterateRights(@NotNull IL leftIndex);

    /**
     * Iterates over all left entities associated with the <code>rightIndex</code> and calls an {@code action} on each.
     */
    default void forEachLeft(@NotNull IR rightIndex, @NotNull Consumer<? super EL> action) {
        try (ResultSetIterator<EL> iterator = iterateLefts(rightIndex)) {
            iterator.forEachRemaining(action);
        }
    }

    /**
     * Iterates over all right entities associated with the <code>leftIndex</code> and calls an {@code action} on each.
     */
    default void forEachRight(@NotNull IL leftIndex, @NotNull Consumer<? super ER> action) {
        try (ResultSetIterator<ER> iterator = iterateRights(leftIndex)) {
            iterator.forEachRemaining(action);
        }
    }

    /**
     * Fetches all left entities associated with the <code>rightIndex</code>.
     */
    default @NotNull List<EL> fetchAllLefts(@NotNull IR rightIndex) {
        try (ResultSetIterator<EL> iterator = iterateLefts(rightIndex)) {
            return Lists.newArrayList(iterator);
        }
    }

    /**
     * Fetches all right entities associated with the <code>leftIndex</code>.
     */
    default @NotNull List<ER> fetchAllRights(@NotNull IL leftIndex) {
        try (ResultSetIterator<ER> iterator = iterateRights(leftIndex)) {
            return Lists.newArrayList(iterator);
        }
    }
}
