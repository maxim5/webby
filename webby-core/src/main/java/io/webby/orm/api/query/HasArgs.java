package io.webby.orm.api.query;

import org.jetbrains.annotations.NotNull;

public interface HasArgs extends Representable {
    @NotNull Args args();
}
