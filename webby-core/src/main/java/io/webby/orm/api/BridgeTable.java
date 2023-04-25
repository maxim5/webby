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
 *     <li>the "left" foreign key holding an {@code IL} id and referencing {@code EL} entities</li>
 *     <li>the "right" foreign key holding an {@code IR} id and referencing {@code ER} entities</li>
 * </ul>
 *
 * @param <IL> the id type of the left entity
 * @param <EL> the left entity type
 * @param <IR> the id type of the right entity
 * @param <ER> the right entity type
 */
public interface BridgeTable<IL, EL, IR, ER> {
    /**
     * Returns whether the table contains a <code>(leftId, rightId)</code> entry.
     */
    boolean exists(@NotNull IL leftId, @NotNull IR rightId);

    /**
     * Returns whether the table contains any association of <code>leftId</code> with any right entity.
     */
    default boolean existsLeft(@NotNull IL leftId) {
        return countRights(leftId) > 0;
    }

    /**
     * Returns whether the table contains any association of <code>rightId</code> with any left entity.
     */
    default boolean existsRight(@NotNull IR rightId) {
        return countLefts(rightId) > 0;
    }

    /**
     * Returns the number of association entries containing the <code>leftId</code>.
     */
    int countLefts(@NotNull IR rightId);

    /**
     * Returns the number of association entries containing the <code>rightId</code>.
     */
    int countRights(@NotNull IL leftId);

    /**
     * Returns an iterator over the left entities associated with the <code>rightId</code>.
     * <b>Important</b>: the caller is responsible for closing the iterator:
     * <pre>
     *     try (ResultSetIterator&lt;Entity&gt; iterator = table.iterateLefts(index)) {
     *         iterator.forEachRemaining(action);
     *     }
     * </pre>
     */
    @MustBeClosed
    @NotNull ResultSetIterator<EL> iterateLefts(@NotNull IR rightId);

    /**
     * Returns an iterator over the right entities associated with the <code>leftId</code>.
     * <b>Important</b>: the caller is responsible for closing the iterator:
     * <pre>
     *     try (ResultSetIterator&lt;Entity&gt; iterator = table.iterateRights(index)) {
     *         iterator.forEachRemaining(action);
     *     }
     * </pre>
     */
    @MustBeClosed
    @NotNull ResultSetIterator<ER> iterateRights(@NotNull IL leftId);

    /**
     * Iterates over all left entities associated with the <code>rightId</code> and calls an {@code action} on each.
     */
    default void forEachLeft(@NotNull IR rightId, @NotNull Consumer<? super EL> action) {
        try (ResultSetIterator<EL> iterator = iterateLefts(rightId)) {
            iterator.forEachRemaining(action);
        }
    }

    /**
     * Iterates over all right entities associated with the <code>leftId</code> and calls an {@code action} on each.
     */
    default void forEachRight(@NotNull IL leftId, @NotNull Consumer<? super ER> action) {
        try (ResultSetIterator<ER> iterator = iterateRights(leftId)) {
            iterator.forEachRemaining(action);
        }
    }

    /**
     * Fetches all left entities associated with the <code>rightId</code>.
     */
    default @NotNull List<EL> fetchAllLefts(@NotNull IR rightId) {
        try (ResultSetIterator<EL> iterator = iterateLefts(rightId)) {
            return Lists.newArrayList(iterator);
        }
    }

    /**
     * Fetches all right entities associated with the <code>leftId</code>.
     */
    default @NotNull List<ER> fetchAllRights(@NotNull IL leftId) {
        try (ResultSetIterator<ER> iterator = iterateRights(leftId)) {
            return Lists.newArrayList(iterator);
        }
    }
}
