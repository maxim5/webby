package io.webby.orm.api;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public interface TableObj<K, E> extends BaseTable<E> {
    @Override
    @NotNull TableObj<K, E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    boolean exists(@NotNull K key);

    @Nullable E getByPkOrNull(@NotNull K key);

    default @NotNull E getByPkOrDie(@NotNull K key) {
        return requireNonNull(getByPkOrNull(key), "Entity not found by PK=" + key);
    }

    default @NotNull Optional<E> getOptionalByPk(@NotNull K key) {
        return Optional.ofNullable(getByPkOrNull(key));
    }

    default @NotNull Map<K, E> getBatchByPk(@NotNull List<K> keys) {
        // The following code is simpler, but doesn't handle nulls correctly.
        //   return keys.stream().collect(Collectors.toMap(k -> k, this::getByPkOrNull));
        // See https://stackoverflow.com/questions/24630963/nullpointerexception-in-collectors-tomap-with-null-entry-values
        HashMap<K, E> map = new HashMap<>();
        keys.forEach(k -> map.put(k, getByPkOrNull(k)));
        return map;
    }

    @NotNull K keyOf(@NotNull E entity);

    @CanIgnoreReturnValue
    int updateByPk(@NotNull E entity);

    @CanIgnoreReturnValue
    default int updateByPkOrInsert(@NotNull E entity) {
        int updated = updateByPk(entity);
        if (updated == 0) {
            return insert(entity);
        }
        return updated;
    }

    @CanIgnoreReturnValue
    int deleteByPk(@NotNull K key);
}
