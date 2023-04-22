package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.webby.orm.api.query.Args.flattenArgsOf;
import static io.webby.orm.api.query.InvalidQueryException.assure;
import static io.webby.orm.api.query.Representables.joinWithCommas;

public class SelectFrom extends Unit {
    private static final String PATTERN = """
        SELECT %s
        FROM %s""";

    private final ImmutableList<Term> terms;

    public SelectFrom(@NotNull String table, @NotNull List<? extends Term> terms) {
        super(PATTERN.formatted(joinWithCommas(terms), table), flattenArgsOf(terms));
        assure(!terms.isEmpty(), "No terms provided for select query: table=%s", table);
        this.terms = ImmutableList.copyOf(terms);
    }

    public @NotNull ImmutableList<Term> terms() {
        return terms;
    }
}
