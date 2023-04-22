package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public class IsNull extends Unit implements BoolTerm {
    public IsNull(@NotNull Term term) {
        super("%s IS NULL".formatted(term.repr()), term.args());
    }

    public static @NotNull IsNull isNull(@NotNull Term term) {
        return new IsNull(term);
    }
}
