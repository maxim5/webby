package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

import static io.webby.orm.api.query.Args.flattenArgsOf;
import static io.webby.orm.api.query.InvalidQueryException.assure;

public class SelectUnion extends Unit implements SelectQuery {
    private final ImmutableList<SelectQuery> selects;

    public SelectUnion(@NotNull ImmutableList<SelectQuery> selects) {
        super(selects.stream().map(Representable::repr).collect(Collectors.joining("UNION\n")), flattenArgsOf(selects));
        assure(!selects.isEmpty(), "No select queries provided");
        assure(selects.size() > 1, "A single select query provided for a union");
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
