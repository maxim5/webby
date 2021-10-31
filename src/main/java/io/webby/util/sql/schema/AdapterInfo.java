package io.webby.util.sql.schema;

import io.webby.util.OneOf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AdapterInfo(@NotNull OneOf<Class<?>, AdapterSignature> oneOf) {
    public static @Nullable AdapterInfo ofClass(@Nullable Class<?> adapterClass) {
        return adapterClass != null ? new AdapterInfo(OneOf.ofFirst(adapterClass)) : null;
    }

    public static @NotNull AdapterInfo ofSignature(@NotNull AdapterSignature signature) {
        return new AdapterInfo(OneOf.ofSecond(signature));
    }

    public boolean hasClass() {
        return oneOf.hasFirst();
    }

    public @Nullable Class<?> klass() {
        return oneOf.first();
    }

    public boolean hasSignature() {
        return oneOf.hasSecond();
    }

    public @Nullable AdapterSignature signature() {
        return oneOf.second();
    }
}
