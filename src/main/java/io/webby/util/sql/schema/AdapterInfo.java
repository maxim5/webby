package io.webby.util.sql.schema;

import io.webby.util.AtomicLazy;
import io.webby.util.OneOf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

import static io.webby.util.EasyClasspath.*;

public class AdapterInfo {
    public static final String CREATE = "createInstance";
    public static final String FILL_VALUES = "fillArrayValues";

    private final @NotNull OneOf<Class<?>, AdapterSignature> oneOf;
    private final AtomicLazy<String> staticClassRef = new AtomicLazy<>();

    private AdapterInfo(@NotNull OneOf<Class<?>, AdapterSignature> oneOf) {
        this.oneOf = oneOf;
    }

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

    public @NotNull String staticRef() {
        return staticClassRef.lazyGet(() -> oneOf.fromEither(AdapterInfo::classToStaticRef, AdapterSignature::className));
    }

    private static @NotNull String classToStaticRef(@NotNull Class<?> klass) {
        String canonicalName = Naming.shortCanonicalName(klass);

        if (hasMethod(klass, method -> isPublicStatic(method) && method.getName().equals(CREATE)) &&
            hasMethod(klass, method -> isPublicStatic(method) && method.getName().equals(FILL_VALUES))) {
            return canonicalName;
        }

        Field staticField = findField(klass, field -> isPublicStatic(field) && field.getType().isAssignableFrom(klass));
        if (staticField != null) {
            return "%s.%s".formatted(canonicalName, staticField);
        }

        return "new %s()".formatted(canonicalName);
    }
}
