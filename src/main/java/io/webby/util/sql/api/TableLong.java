package io.webby.util.sql.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public interface TableLong<E> extends TableObj<Long, E> {
    @Override
    @NotNull TableLong<E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    @Nullable E getByPkOrNull(long key);

    default @NotNull E getByPkOrDie(long key) {
        return Objects.requireNonNull(getByPkOrNull(key), "Entity not found by PK=" + key);
    }

    default @NotNull Optional<E> getOptionalByPk(long key) {
        return Optional.ofNullable(getByPkOrNull(key));
    }

    @Override
    default @Nullable E getByPkOrNull(@NotNull Long key) {
        return getByPkOrNull(key.longValue());
    }

    @Override
    default @NotNull E getByPkOrDie(@NotNull Long key) {
        return getByPkOrDie(key.longValue());
    }

    @Override
    default @NotNull Optional<E> getOptionalByPk(@NotNull Long key) {
        return getOptionalByPk(key.longValue());
    }
}
