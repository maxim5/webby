package io.webby.util.sql.arch;

import org.jetbrains.annotations.NotNull;

public interface JavaNameHolder {
    @NotNull String javaName();

    @NotNull String packageName();
}
