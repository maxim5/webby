package io.webby.orm.api;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.webby.orm.api.query.CompositeClause;
import io.webby.orm.api.query.Limit;
import io.webby.orm.api.query.Offset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public interface TableObj<K, E> extends BaseTable<E> {
    @Override
    @NotNull TableObj<K, E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    @Nullable E getByPkOrNull(@NotNull K key);

    default @NotNull E getByPkOrDie(@NotNull K key) {
        return requireNonNull(getByPkOrNull(key), "Entity not found by PK=" + key);
    }

    default @NotNull Optional<E> getOptionalByPk(@NotNull K key) {
        return Optional.ofNullable(getByPkOrNull(key));
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

    default @NotNull Page<E> fetchPage(@NotNull CompositeClause clause) {
        List<E> items = fetchMatching(clause);
        Offset offset = clause.offset();
        Limit limit = clause.limit();
        boolean isFullPage = limit != null && items.size() == limit.limitValue();
        if (isFullPage) {
            K lastItem = keyOf(items.get(items.size() - 1));
            int tokenOffset = offset != null ? offset.offsetValue() + limit.limitValue() : PageToken.NO_OFFSET;
            PageToken pageToken = new PageToken(String.valueOf(lastItem), tokenOffset);
            return new Page<>(items, pageToken);
        }
        return new Page<>(items, null);
    }
}
