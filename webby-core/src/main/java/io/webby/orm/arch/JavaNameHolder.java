package io.webby.orm.arch;

import org.jetbrains.annotations.NotNull;

public interface JavaNameHolder {
    @NotNull String javaName();

    @NotNull String packageName();
}
