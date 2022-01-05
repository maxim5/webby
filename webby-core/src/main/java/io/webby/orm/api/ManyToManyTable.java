package io.webby.orm.api;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.MustBeClosed;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public interface ManyToManyTable<IL, EL, IR, ER> {
    boolean exists(@NotNull IL leftIndex, @NotNull IR rightIndex);

    default boolean leftExists(@NotNull IL leftIndex) {
        return countRights(leftIndex) == 0;
    }

    default boolean rightExists(@NotNull IR rightIndex) {
        return countLefts(rightIndex) == 0;
    }

    int countRights(@NotNull IL leftIndex);

    @MustBeClosed
    @NotNull ResultSetIterator<ER> iterateRights(@NotNull IL leftIndex);

    default void forEachRight(@NotNull IL leftIndex, @NotNull Consumer<? super ER> action) {
        try (ResultSetIterator<ER> iterator = iterateRights(leftIndex)) {
            iterator.forEachRemaining(action);
        }
    }

    default @NotNull List<ER> fetchAllRights(@NotNull IL leftIndex) {
        try (ResultSetIterator<ER> iterator = iterateRights(leftIndex)) {
            return Lists.newArrayList(iterator);
        }
    }

    int countLefts(@NotNull IR rightIndex);

    @MustBeClosed
    @NotNull ResultSetIterator<EL> iterateLefts(@NotNull IR rightIndex);

    default void forEachLeft(@NotNull IR rightIndex, @NotNull Consumer<? super EL> action) {
        try (ResultSetIterator<EL> iterator = iterateLefts(rightIndex)) {
            iterator.forEachRemaining(action);
        }
    }

    default @NotNull List<EL> fetchAllLefts(@NotNull IR rightIndex) {
        try (ResultSetIterator<EL> iterator = iterateLefts(rightIndex)) {
            return Lists.newArrayList(iterator);
        }
    }
}
