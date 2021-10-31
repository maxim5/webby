package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;

public record AdapterSignature(@NotNull Class<?> fieldType) {
    public @NotNull String className() {
        return defaultAdapterName(fieldType);
    }

    public static @NotNull String defaultAdapterName(@NotNull Class<?> klass) {
        return "%sJdbcAdapter".formatted(klass.getSimpleName());
    }
}
