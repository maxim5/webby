package io.webby.util.sql.api;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public interface TableObj<K, E> extends BaseTable<E> {
    @Nullable E getByPkOrNull(@NotNull K key, @NotNull FollowReferences follow);

    default @Nullable E getByPkOrNull(@NotNull K key) {
        return getByPkOrNull(key, FollowReferences.NO_FOLLOW);
    }

    default @NotNull E getByPkOrDie(@NotNull K key) {
        return Objects.requireNonNull(getByPkOrNull(key), "Entity not found by PK=" + key);
    }

    default @NotNull Optional<E> getOptionalByPk(@NotNull K key) {
        return Optional.ofNullable(getByPkOrNull(key));
    }

    @CanIgnoreReturnValue
    int updateByPk(@NotNull E entity);
}
