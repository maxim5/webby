package io.webby.util.sql.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public interface TableInt<E> extends TableObj<Integer, E> {
    @Override
    @NotNull TableInt<E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    @Nullable E getByPkOrNull(int key);

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

    int intKeyOf(@NotNull E entity);

    default @NotNull Integer keyOf(@NotNull E entity) {
        return intKeyOf(entity);
    }
}
