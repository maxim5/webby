package io.webby.util.sql.codegen;

import io.webby.util.sql.schema.*;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static io.webby.util.sql.codegen.ColumnJoins.joinWithTransform;
import static io.webby.util.sql.codegen.ColumnJoins.linesJoiner;
import static java.util.Objects.requireNonNull;

class ResultSetConversionMaker {
    private static final Set<Class<?>> FULL_NAME_CLASSES = Set.of(java.util.Date.class, java.sql.Date.class);
    private int columnIndex = 0;

    public @NotNull String make(@NotNull TableSchema table) {
        return table.fields().stream().map(this::assignFieldLine).collect(linesJoiner(BaseCodegen.INDENT));
    }

    private @NotNull String assignFieldLine(@NotNull TableField field) {
        Class<?> fieldType = field.javaType();
        String fieldClassName = FULL_NAME_CLASSES.contains(fieldType) ?
                fieldType.getName() :
                Naming.shortCanonicalName(fieldType);
        return "%s %s = %s;".formatted(fieldClassName, field.javaName(), createExpr(field));
    }

    private @NotNull String createExpr(@NotNull TableField field) {
        if (field.isNativelySupportedType()) {
            assert field instanceof OneColumnTableField : "Native field is not one column: %s".formatted(field);
            return resultSetGetterExpr(((OneColumnTableField) field).column(), ++columnIndex);
        } else {
            String staticRef = requireNonNull(field.adapterInfo()).staticRef();
            String params = field instanceof OneColumnTableField simpleField ?
                    resultSetGetterExpr(simpleField.column(), ++columnIndex) :
                    joinWithTransform(field.columns(), column -> resultSetGetterExpr(column, ++columnIndex));
            return "%s.createInstance(%s)".formatted(staticRef, params);
        }
    }

    private static @NotNull String resultSetGetterExpr(@NotNull Column column, int columnIndex) {
        String getter = column.type().jdbcType().getterMethod();
        return "result.%s(start+%d)".formatted(getter, columnIndex);
    }
}
