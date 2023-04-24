package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import io.webby.util.collect.EasyIterables;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.webby.orm.api.query.Args.flattenArgsOf;
import static io.webby.orm.api.query.InvalidQueryException.assure;

/**
 * A <code>UNION</code> of several {@link SelectQuery} statements.
 */
@Immutable
public class SelectUnion extends Unit implements TypedSelectQuery {
    private final ImmutableList<SelectQuery> selects;
    private final int columnsNumber;

    public SelectUnion(@NotNull ImmutableList<SelectQuery> selects) {
        super(selects.stream().map(Representables::trimmed).collect(Collectors.joining("\nUNION\n")), flattenArgsOf(selects));
        assure(!selects.isEmpty(), "No select queries provided");
        assure(selects.size() > 1, "A single select query provided for a union: %s", selects);

        List<TypedSelectQuery> typedSelects = asTypedSelectQueries(selects);
        this.selects = selects;
        this.columnsNumber = typedSelects.stream().map(TypedSelectQuery::columnsNumber).findFirst()
            .orElseThrow(() -> new InvalidQueryException("Failed to detect columns number: %s", selects));
    }

    private static @NotNull List<TypedSelectQuery> asTypedSelectQueries(@NotNull ImmutableList<SelectQuery> selects) {
        List<TypedSelectQuery> typedSelects = selects.stream()
            .map(query -> query instanceof TypedSelectQuery typed ? typed : null)
            .filter(Objects::nonNull)
            .toList();
        assure(!typedSelects.isEmpty(), "Provided select queries are all non-typed: %s", selects);
        assure(EasyIterables.allEqual(typedSelects.stream().map(TypedSelectQuery::columnsNumber)),
               "Provided select queries have different number of columns: %s", selects);
        return typedSelects;
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Override
    public int columnsNumber() {
        return columnsNumber;
    }

    @Override
    public @NotNull List<TermType> columnTypes() {
        return Collections.nCopies(columnsNumber, TermType.WILDCARD);  // detect from the typed queries?
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
