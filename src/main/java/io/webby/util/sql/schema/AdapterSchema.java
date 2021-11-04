package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;

public record AdapterSchema(@NotNull AdapterSignature signature) implements JavaNameHolder {
    @Override
    public @NotNull String javaName() {
        return signature().className();
    }

    @Override
    public @NotNull String packageName() {
        return signature.packageName();
    }
}
