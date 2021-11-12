package io.webby.util.sql.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public interface TableInt<E> extends TableObj<Integer, E> {
    @Nullable E getByPkOrNull(int key, @NotNull FollowReferences follow);

    default @Nullable E getByPkOrNull(int key) {
        return getByPkOrNull(key, FollowReferences.NO_FOLLOW);
    }

    default @NotNull E getByPkOrDie(int key) {
        return Objects.requireNonNull(getByPkOrNull(key), "Entity not found by PK=" + key);
    }

    default @NotNull Optional<E> getOptionalByPk(int key) {
        return Optional.ofNullable(getByPkOrNull(key));
    }

    @Override
    default @Nullable E getByPkOrNull(@NotNull Integer key) {
        return getByPkOrNull(key.intValue());
    }

    @Override
    default @NotNull E getByPkOrDie(@NotNull Integer key) {
        return getByPkOrDie(key.intValue());
    }

    @Override
    default @NotNull Optional<E> getOptionalByPk(@NotNull Integer key) {
        return getOptionalByPk(key.intValue());
    }

    @Override
    default @Nullable E getByPkOrNull(@NotNull Integer key, @NotNull FollowReferences follow) {
        return getByPkOrNull(key.intValue(), follow);
    }
}
