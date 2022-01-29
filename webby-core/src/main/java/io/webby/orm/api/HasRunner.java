package io.webby.orm.api;

import org.jetbrains.annotations.NotNull;

public interface HasRunner {
    @NotNull QueryRunner runner();
}
