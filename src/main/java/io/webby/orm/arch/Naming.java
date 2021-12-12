package io.webby.orm.arch;

import com.google.common.base.CaseFormat;
import io.webby.util.base.EasyStrings;
import org.jetbrains.annotations.NotNull;

public class Naming {
    @SuppressWarnings("ConstantConditions")
    public static @NotNull String camelToSnake(@NotNull String s) {
        return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(s);
    }

    @SuppressWarnings("ConstantConditions")
    public static @NotNull String camelUpperToLower(@NotNull String s) {
        return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_CAMEL).convert(s);
    }

    @SuppressWarnings("ConstantConditions")
    public static @NotNull String camelLowerToUpper(@NotNull String s) {
        return CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL).convert(s);
    }

    public static @NotNull String generatedSimpleJavaName(@NotNull Class<?> model) {
        assert !model.isAnonymousClass() : "Invalid model class: %s".formatted(model);
        if (model.isMemberClass()) {
            return shortCanonicalJavaName(model).replace('.', '_');
        }
        return model.getSimpleName();
    }

    public static @NotNull String shortCanonicalJavaName(@NotNull Class<?> type) {
        assert !type.isAnonymousClass() : "Canonical java name not available for: %s".formatted(type);
        if (type.isMemberClass()) {
            String withoutPackage = EasyStrings.removePrefix(type.getCanonicalName(), type.getPackageName());
            return EasyStrings.removePrefix(withoutPackage, ".");
        }
        return type.getSimpleName();
    }

    public static @NotNull String defaultAdapterName(@NotNull Class<?> model) {
        assert !model.isAnonymousClass() : "Invalid model class: %s".formatted(model);
        if (model.isMemberClass()) {
            return "%s_JdbcAdapter".formatted(generatedSimpleJavaName(model));
        }
        return "%sJdbcAdapter".formatted(model.getSimpleName());
    }

    public static @NotNull String defaultModelClassName(@NotNull String adapterName) {
        assert adapterName.endsWith("JdbcAdapter") : "Unexpected adapter name: %s".formatted(adapterName);
        return EasyStrings.removeSuffix(adapterName, "JdbcAdapter");
    }
}
