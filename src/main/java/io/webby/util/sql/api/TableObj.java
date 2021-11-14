package io.webby.util.sql.api;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public interface TableObj<K, E> extends BaseTable<E> {
    @Override
    @NotNull TableObj<K, E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    @Nullable E getByPkOrNull(@NotNull K key);

    default @NotNull E getByPkOrDie(@NotNull K key) {
        return Objects.requireNonNull(getByPkOrNull(key), "Entity not found by PK=" + key);
    }

    default @NotNull Optional<E> getOptionalByPk(@NotNull K key) {
        return Optional.ofNullable(getByPkOrNull(key));
    }

    @NotNull K keyOf(@NotNull E entity);

    @CanIgnoreReturnValue
    int updateByPk(@NotNull E entity);
}
