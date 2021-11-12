package io.webby.util.sql.api;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.auth.session.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

public interface TableObj<K, E> extends BaseTable<E> {
    @Nullable E getByPkOrNull(@NotNull K key);

    default @NotNull E getByPkOrDie(@NotNull K key) {
        return Objects.requireNonNull(getByPkOrNull(key), "Entity not found by PK=" + key);
    }

    default @NotNull Optional<E> getOptionalByPk(@NotNull K key) {
        return Optional.ofNullable(getByPkOrNull(key));
    }

    default @Nullable E getByPkOrNull(@NotNull K key, @NotNull FollowReferences follow) {
        return getByPkOrNull(key);  // TODO: temp
    }

    @CanIgnoreReturnValue
    int updateByPk(@NotNull E entity);
}
