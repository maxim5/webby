package io.webby.orm.arch;

import org.jetbrains.annotations.NotNull;

public record AdapterArch(@NotNull PojoArch pojoArch) implements JavaNameHolder {
    @Override
    public @NotNull String javaName() {
        return pojoArch.adapterName();
    }

    @Override
    public @NotNull String packageName() {
        return pojoArch.pojoType().getPackageName();
    }
}
