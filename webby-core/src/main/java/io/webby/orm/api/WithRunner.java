package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;

public interface WithRunner {
    @NotNull QueryRunner runner();
}
