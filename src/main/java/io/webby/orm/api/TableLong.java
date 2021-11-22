package io.webby.orm.api;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public interface TableLong<E> extends TableObj<Long, E> {
    @Override
    @NotNull TableLong<E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    @Nullable E getByPkOrNull(long key);

    default @NotNull E getByPkOrDie(long key) {
        return requireNonNull(getByPkOrNull(key), "Entity not found by PK=" + key);
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

    long insertAutoIncPk(@NotNull E entity);

    long longKeyOf(@NotNull E entity);

    default @NotNull Long keyOf(@NotNull E entity) {
        return longKeyOf(entity);
    }

    @CanIgnoreReturnValue
    int deleteByPk(long key) ;

    @CanIgnoreReturnValue
    default int deleteByPk(@NotNull Long key) {
        return deleteByPk(key.longValue());
    }
}