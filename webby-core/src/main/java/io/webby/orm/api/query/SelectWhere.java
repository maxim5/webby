package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import io.webby.orm.api.BaseTable;
import io.webby.orm.api.Engine;
import io.webby.orm.api.TableMeta;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static io.webby.orm.api.query.Args.flattenArgsOf;
import static io.webby.orm.api.query.Representables.joinWithLines;

public class SelectWhere extends Unit implements SelectQuery {
    private final ImmutableList<Term> columnTerms;

    public SelectWhere(@NotNull SelectFrom selectFrom, @NotNull CompositeFilter clause) {
        super(joinWithLines(selectFrom, clause), flattenArgsOf(selectFrom, clause));
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
        private final ImmutableList.Builder<Term> terms = ImmutableList.builder();
        private final CompositeFilter.Builder filter = new CompositeFilter.Builder();
    
        public Builder(@NotNull String table) {
            this.table = table;
        }
    
        public @NotNull Builder select(@NotNull Term term) {
            terms.add(term);
            return this;
        }
    
        public @NotNull Builder select(@NotNull Term term1, @NotNull Term term2) {
            terms.add(term1, term2);
            return this;
        }
    
        public @NotNull Builder select(@NotNull Term @NotNull ... terms) {
            this.terms.add(terms);
            return this;
        }
    
        public @NotNull Builder select(@NotNull Iterable<Term> terms) {
            this.terms.addAll(terms);
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
    
        public @NotNull Builder with(@NotNull LimitClause limit) {
            filter.with(limit);
            return this;
        }
    
        public @NotNull Builder with(@NotNull Offset offset) {
            filter.with(offset);
            return this;
        }
    
        public @NotNull Builder with(@NotNull Pagination pagination, @NotNull Engine engine) {
            filter.with(pagination, engine);
            return this;
        }
    
        public @NotNull Builder applying(@NotNull Consumer<Builder> consumer) {
            consumer.accept(this);
            return this;
        }
    
        public @NotNull SelectWhere build() {
            return new SelectWhere(new SelectFrom(table, terms.build()), filter.build());
        }
    }
}
