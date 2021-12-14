package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import io.webby.util.lazy.AtomicLazy;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.webby.orm.api.query.Units.flattenArgsOf;
import static io.webby.orm.api.query.Units.joinWithCommas;

public class SelectFrom extends UnitLazy {
    private final ImmutableList<Term> terms;
    private final AtomicLazy<String> tableRef = new AtomicLazy<>(null);

    public SelectFrom(@NotNull List<? extends Term> terms) {
        super(flattenArgsOf(terms));
        this.terms = ImmutableList.copyOf(terms);
    }

    public static @NotNull SelectFrom of(@NotNull Term term) {
        return new SelectFrom(ImmutableList.of(term));
    }

    public static @NotNull SelectFrom of(@NotNull Term term1, @NotNull Term term2) {
        return new SelectFrom(ImmutableList.of(term1, term2));
    }

    public static @NotNull SelectFrom of(@NotNull Term @NotNull ... terms) {
        return new SelectFrom(ImmutableList.copyOf(terms));
    }

    public void withTable(@NotNull String tableName) {
        tableRef.initializeOrCompare(tableName);
    }

    @Override
    protected @NotNull String supplyRepr() {
        return """
        SELECT %s
        FROM %s\
        """.formatted(joinWithCommas(terms), tableRef.getOrDie());
    }
}
