package io.webby.util.sql.api.query;

import org.jetbrains.annotations.NotNull;

public interface Term extends Repr {
    @NotNull TermType type();
}
