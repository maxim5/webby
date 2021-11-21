package io.webby.util.sql.api.query;

import org.jetbrains.annotations.NotNull;

public enum TermType {
    NUMBER,
    STRING,
    BOOL,
    WILDCARD;

    public static boolean match(@NotNull TermType first, @NotNull TermType second) {
        return first == second || first == WILDCARD || second == WILDCARD;
    }

    public static boolean match(@NotNull TermType first, @NotNull TermType second, @NotNull TermType third) {
        return match(first, second) && match(second, third) && match(first, third);
    }
}
