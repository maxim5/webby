package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

/**
 * A wrapper around any term of type {@link TermType#BOOL} type that inherits from {@link BoolTerm}.
 */
public class BoolWrapper extends Unit implements BoolTerm {
    public BoolWrapper(@NotNull Term term) {
        super(term.repr(), term.args());
        assert term.type() == TermType.BOOL : "Term can't be used for boolean: %s".formatted(term);
    }
}
