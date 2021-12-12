package io.webby.orm.arch;

import com.google.common.base.CaseFormat;
import io.webby.util.base.EasyStrings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

public class Naming {
    public static @NotNull String fieldSqlName(@NotNull String fieldName) {
        return cleanupSql(camelToSnake(fieldName));
    }

    public static @NotNull String fieldSqlName(@NotNull String fieldNamePart1, @NotNull String fieldNamePart2) {
        return "%s_%s".formatted(fieldSqlName(fieldNamePart1), fieldSqlName(fieldNamePart2));
    }

    public static @NotNull String modelSqlName(@NotNull String modelName) {
        return cleanupSql(camelToSnake(modelName));
    }

    private static @NotNull String cleanupSql(@NotNull String name) {
        return name.replaceAll("_+", "_").replaceAll("^_", "").replaceAll("_$", "");
    }

    public static @NotNull String generatedSimpleJavaName(@NotNull Class<?> model) {
        assert !model.isAnonymousClass() : "Invalid model class: %s".formatted(model);
        if (model.isMemberClass()) {
            return shortCanonicalJavaName(model).replace('.', '_');
        }
        return model.getSimpleName();
    }

    public static @NotNull String generatedJavaTableName(@NotNull String modelName) {
        return "%sTable".formatted(modelName);
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

    @VisibleForTesting
    @SuppressWarnings("ConstantConditions")
    static @NotNull String camelToSnake(@NotNull String s) {
        return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(s);
    }

    @SuppressWarnings("ConstantConditions")
    static @NotNull String camelUpperToLower(@NotNull String s) {
        return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_CAMEL).convert(s);
    }

    @SuppressWarnings("ConstantConditions")
    static @NotNull String camelLowerToUpper(@NotNull String s) {
        return CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL).convert(s);
    }
}
