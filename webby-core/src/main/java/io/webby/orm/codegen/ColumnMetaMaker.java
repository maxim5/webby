package io.webby.orm.codegen;

import io.webby.orm.api.TableMeta.ConstraintStatus;
import io.webby.orm.arch.Column;
import io.webby.orm.arch.model.TableField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ColumnMetaMaker {
    public static @NotNull String makeColumnMeta(@NotNull TableField field, @NotNull Column column) {
        String sqlName = column.sqlName();
        Class<?> nativeType = column.type().jdbcType().nativeType();
        String type = nativeType.isPrimitive() || nativeType == String.class ?
            nativeType.getSimpleName() :
            nativeType == byte[].class ?
                "byte[]" :
                nativeType.getName();

        return SnippetLine.of(
            "ColumnMeta.of(OwnColumn.%s, %s.class)".formatted(sqlName, type),
            toConstraintStatus(".withPrimaryKey", field.isPrimaryKey(), field.isMultiColumn()),
            toConstraintStatus(".withUnique", field.isUnique(), field.isMultiColumn()),
            toBooleanStatus(".withForeignKey", field.isForeignKey()),
            toBooleanStatus(".withNullable", field.isNullable()),
            toStringStatus(".withDefault", field.columnDefault(column))
        ).join();
    }

    private static @NotNull String toConstraintStatus(@NotNull String method, boolean value, boolean isMultiColumn) {
        ConstraintStatus status = isMultiColumn ? ConstraintStatus.COMPOSITE : ConstraintStatus.SINGLE_COLUMN;
        return value ? "%s(ConstraintStatus.%s)".formatted(method, status) : "";
    }

    private static @NotNull String toBooleanStatus(@NotNull String method, boolean value) {
        return value ? "%s(true)".formatted(method) : "";
    }

    private static @NotNull String toStringStatus(@NotNull String method, @Nullable String value) {
        return value != null ? "%s(\"%s\")".formatted(method, value) : "";
    }
}
