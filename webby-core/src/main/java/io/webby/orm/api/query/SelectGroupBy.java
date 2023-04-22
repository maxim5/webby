package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import io.webby.orm.api.BaseTable;
import io.webby.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static io.webby.orm.api.query.Args.flattenArgsOf;
import static io.webby.orm.api.query.Representables.joinWithLines;

public class SelectGroupBy extends Unit implements SelectQuery {
    private final ImmutableList<Term> columnTerms;

    public SelectGroupBy(@NotNull SelectFrom selectFrom, @Nullable Where where,
                         @NotNull GroupBy groupBy, @Nullable Having having, @Nullable OrderBy orderBy) {
        super(joinWithLines(selectFrom, where, groupBy, having, orderBy),
              flattenArgsOf(selectFrom, where, groupBy, having, orderBy));
        columnTerms = selectFrom.terms();
    }

    public static @NotNull Builder from(@NotNull String table) {
        return new Builder(table);
    }

    public static @NotNull Builder from(@NotNull TableMeta meta) {
        return from(meta.sqlTableName());
    }

    public static @NotNull Builder from(@NotNull BaseTable<?> table) {
        return from(table.meta());
    }

    @Override
    public int columnsNumber() {
        return columnTerms.size();
    }

    public static class Builder {
        private final String table;
        private FuncExpr funcExpr = null;
        private final ImmutableList.Builder<Named> terms = ImmutableList.builder();
        private Having having;
        private final CompositeFilter.Builder filter = new CompositeFilter.Builder();
    
        public Builder(@NotNull String table) {
            this.table = table;
        }
    
        public @NotNull Builder select(@NotNull Named term, @NotNull FuncExpr aggregate) {
            return groupBy(term).aggregate(aggregate);
        }
    
        public @NotNull Builder select(@NotNull List<Named> terms, @NotNull FuncExpr aggregate) {
            return groupBy(terms).aggregate(aggregate);
        }
    
        public @NotNull Builder aggregate(@NotNull FuncExpr aggregate) {
            assert funcExpr == null : "Aggregate function already set: %s".formatted(funcExpr);
            assert aggregate.isAggregate() : "Non-aggregate function supplied: %s".formatted(aggregate);
            funcExpr = aggregate;
            return this;
        }
    
        public @NotNull Builder groupBy(@NotNull Named term) {
            terms.add(term);
            return this;
        }
    
        public @NotNull Builder groupBy(@NotNull Named term1, @NotNull Named term2) {
            terms.add(term1, term2);
            return this;
        }
    
        public @NotNull Builder groupBy(@NotNull Named @NotNull ... terms) {
            this.terms.add(terms);
            return this;
        }
    
        public @NotNull Builder groupBy(@NotNull Iterable<Named> terms) {
            this.terms.addAll(terms);
            return this;
        }
    
        public @NotNull Builder having(@NotNull Having having) {
            this.having = having;
            return this;
        }
    
        public @NotNull Builder where(@NotNull Where where) {
            filter.with(where);
            return this;
        }
    
        public @NotNull Builder orderBy(@NotNull OrderBy orderBy) {
            filter.with(orderBy);
            return this;
        }
    
        public @NotNull Builder applying(@NotNull Consumer<Builder> consumer) {
            consumer.accept(this);
            return this;
        }
    
        public @NotNull SelectGroupBy build() {
            ImmutableList<Named> groupByTerms = terms.build();
            ImmutableList<Term> allTerms = ImmutableList.<Term>builder().addAll(groupByTerms).add(funcExpr).build();
            CompositeFilter composite = filter.build();
            return new SelectGroupBy(new SelectFrom(table, allTerms), composite.where(),
                                     new GroupBy(groupByTerms), having, composite.orderBy());
        }
    }
}
