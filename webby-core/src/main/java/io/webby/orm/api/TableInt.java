package io.webby.orm.api;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntContainer;
import com.carrotsearch.hppc.IntObjectMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.orm.api.query.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public interface TableInt<E> extends TableObj<Integer, E> {
    @Override
    @NotNull TableInt<E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    @Nullable E getByPkOrNull(int key);

    default @NotNull E getByPkOrDie(int key) {
        return requireNonNull(getByPkOrNull(key), "Entity not found by PK=" + key);
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

    @NotNull IntObjectMap<E> getBatchByPk(@NotNull IntContainer keys);

    @NotNull IntArrayList fetchPks(@NotNull Filter filter);

    int insertAutoIncPk(@NotNull E entity);

    int intKeyOf(@NotNull E entity);

    default @NotNull Integer keyOf(@NotNull E entity) {
        return intKeyOf(entity);
    }

    @CanIgnoreReturnValue
    int deleteByPk(int key);

    @CanIgnoreReturnValue
    default int deleteByPk(@NotNull Integer key) {
        return deleteByPk(key.intValue());
    }
}
