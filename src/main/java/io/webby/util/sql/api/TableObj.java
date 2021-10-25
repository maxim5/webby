package io.webby.util.sql.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface TableObj<K, E> extends BaseTable<E> {
    @Nullable E getByPkOrNull(@NotNull K key);

    @NotNull E getByPkOrDie(@NotNull K key);

    @NotNull Optional<E> getOptionalByPk(@NotNull K key);

    int updateByPk(@NotNull E entity);
}
