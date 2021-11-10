package io.webby.util.sql.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface TableLong<E> extends TableObj<Long, E> {
    @Nullable E getByPkOrNull(long key);

    @Override
    default @Nullable E getByPkOrNull(@NotNull Long key) {
        return getByPkOrNull(key.longValue());
    }

    @NotNull E getByPkOrDie(long key);

    @Override
    default @NotNull E getByPkOrDie(@NotNull Long key) {
        return getByPkOrDie(key.longValue());
    }

    @NotNull Optional<E> getOptionalByPk(long key);

    @Override
    default @NotNull Optional<E> getOptionalByPk(@NotNull Long key) {
        return getOptionalByPk(key.longValue());
    }

    @Override
    default @Nullable E getByPkOrNull(@NotNull Long key, @NotNull FollowReferences follow) {
        return getByPkOrNull(key.longValue(), follow);
    }

    default @Nullable E getByPkOrNull(long key, @NotNull FollowReferences follow) {
        return getByPkOrNull(key); // TODO: temp
    }
}
