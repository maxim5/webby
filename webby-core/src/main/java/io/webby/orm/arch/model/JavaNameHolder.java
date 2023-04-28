package io.webby.orm.arch.model;

import org.jetbrains.annotations.NotNull;

public interface JavaNameHolder {
    @NotNull String javaName();

    @NotNull String packageName();
}
