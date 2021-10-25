package io.webby.util.sql.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface TableInt<E> extends TableObj<Integer, E> {
    @Nullable E getByPkOrNull(int key);

    @Override
    default @Nullable E getByPkOrNull(@NotNull Integer key) {
        return getByPkOrNull(key.intValue());
    }

    @NotNull E getByPkOrDie(int key);

    @Override
    default @NotNull E getByPkOrDie(@NotNull Integer key) {
        return getByPkOrDie(key.intValue());
    }

    @NotNull Optional<E> getOptionalByPk(int key);

    @Override
    default @NotNull Optional<E> getOptionalByPk(@NotNull Integer key) {
        return getOptionalByPk(key.intValue());
    }
}
