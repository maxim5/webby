package io.webby.util.sql.schema;

import io.webby.util.sql.DataClassAdaptersLocator;
import org.jetbrains.annotations.NotNull;

public record AdapterSignature(@NotNull Class<?> fieldType) {
    public @NotNull String className() {
        return DataClassAdaptersLocator.defaultAdapterName(fieldType);
    }

    public @NotNull String packageName() {
        return fieldType.getPackageName();
    }
}
