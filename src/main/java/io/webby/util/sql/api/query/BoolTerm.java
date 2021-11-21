package io.webby.util.sql.api.query;

import org.jetbrains.annotations.NotNull;

public interface BoolTerm extends Term {
    @Override
    default @NotNull TermType type() {
        return TermType.BOOL;
    }
}
