package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;

public interface WithEngine {
    @NotNull Engine engine();
}
