package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ArgsHolder extends Representable {
    @NotNull List<Object> args();

    default @NotNull List<Object> argsAssertingResolved() {
        return args();
    }
}
