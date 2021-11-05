package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;

public record AdapterSchema(@NotNull PojoSchema pojoSchema) implements JavaNameHolder {
    @Override
    public @NotNull String javaName() {
        return pojoSchema.adapterName();
    }

    @Override
    public @NotNull String packageName() {
        return pojoSchema.type().getPackageName();
    }
}
