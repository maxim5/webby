package io.webby.orm.api;

import com.google.common.collect.Lists;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.MustBeClosed;
import io.webby.orm.api.query.Clause;
import io.webby.orm.api.query.CompositeClause;
import io.webby.orm.api.query.Limit;
import io.webby.orm.api.query.Offset;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface BaseTable<E> extends Iterable<E> {
    @NotNull BaseTable<E> withReferenceFollowOnRead(@NotNull ReadFollow follow);

    @NotNull Engine engine();

    int count();

    int count(@NotNull Clause clause);

    default boolean isEmpty() {
        return count() == 0;
    }

    @Override
    @MustBeClosed
    @NotNull ResultSetIterator<E> iterator();

    @MustBeClosed
    @NotNull ResultSetIterator<E> iterator(@NotNull Clause clause);

    default @NotNull List<E> fetchAll() {
        try (ResultSetIterator<E> iterator = iterator()) {
            return Lists.newArrayList(iterator);
        }
    }

    default @NotNull List<E> fetchMatching(@NotNull Clause clause) {
        try (ResultSetIterator<E> iterator = iterator(clause)) {
            return Lists.newArrayList(iterator);
        }
    }

    default @NotNull Page<E> fetchPage(@NotNull CompositeClause clause) {
        List<E> items = fetchMatching(clause);
        Offset offset = clause.offset();
        Limit limit = clause.limit();
        boolean isFullPage = limit != null && offset != null && items.size() == limit.limitValue();
        if (isFullPage) {
            PageToken pageToken = new PageToken(null, offset.offsetValue() + limit.limitValue());
            return new Page<>(items, pageToken);
        }
        return new Page<>(items, null);
    }

    @CanIgnoreReturnValue
    int insert(@NotNull E item);

    @NotNull TableMeta meta();
}
