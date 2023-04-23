package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

import static io.webby.orm.api.query.Args.flattenArgsOf;
import static io.webby.orm.api.query.InvalidQueryException.assure;

/**
 * A <code>UNION</code> of several {@link SelectQuery} statements.
 */
@Immutable
public class SelectUnion extends Unit implements SelectQuery {
    private final ImmutableList<SelectQuery> selects;

    public SelectUnion(@NotNull ImmutableList<SelectQuery> selects) {
        super(selects.stream().map(Representables::trimmed).collect(Collectors.joining("\nUNION\n")), flattenArgsOf(selects));
        assure(!selects.isEmpty(), "No select queries provided");
        assure(selects.size() > 1, "A single select query provided for a union: %s", selects);
        assure(selects.stream().map(SelectQuery::columnsNumber).collect(Collectors.toSet()).size() == 1,
               "Provided select queries have different number of columns: %s", selects);
        this.selects = selects;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Override
    public int columnsNumber() {
        return selects.get(0).columnsNumber();
    }

    public @NotNull Builder toBuilder() {
        return new Builder().with(selects);
    }

    public static class Builder {
        private final ImmutableList.Builder<SelectQuery> selects = new ImmutableList.Builder<>();

        public @NotNull Builder with(@NotNull SelectQuery query) {
            selects.add(query);
            return this;
        }

        public @NotNull Builder with(@NotNull Iterable<SelectQuery> queries) {
            selects.addAll(queries);
            return this;
        }

        public @NotNull SelectUnion build() {
            return new SelectUnion(selects.build());
        }
    }
}
