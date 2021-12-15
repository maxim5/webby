package io.webby.orm.api.query;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static io.webby.orm.api.query.Units.flattenArgsOf;
import static io.webby.orm.api.query.Units.joinWithCommas;

public class SelectFrom extends Unit {
    private static final String PATTERN = """
        SELECT %s
        FROM %s""";

    private final ImmutableList<Term> terms;

    public SelectFrom(@NotNull String table, @NotNull List<? extends Term> terms) {
        super(PATTERN.formatted(joinWithCommas(terms), table), flattenArgsOf(terms));
        this.terms = ImmutableList.copyOf(terms);
    }

    public @NotNull ImmutableList<Term> terms() {
        return terms;
    }

    public int termsNumber() {
        return terms.size();
    }
}
