package io.webby.util.sql;

import com.google.common.base.CaseFormat;
import com.google.common.primitives.Primitives;
import io.webby.util.EasyMaps;
import io.webby.util.sql.schema.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SchemaFactory {
    public static @NotNull TableSchema buildTableSchema(@NotNull DataClassInput input) {
        Class<?> dataClass = input.dataClass;
        String dataName = input.dataName;
        String sqlName = camelToSnake(dataName);
        String javaName = "%sTable".formatted(dataName);
        List<TableField> fields = Arrays.stream(dataClass.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(field -> buildTableField(field, dataName))
                .toList();
        return new TableSchema(sqlName, javaName, dataName, dataClass, fields);
    }

    public static @NotNull TableField buildTableField(@NotNull Field field, @NotNull String dataName) {
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();

        Method getter = findGetterMethod(field);
        assert getter != null : "No getter found for field: %s".formatted(field);
        boolean isPrimaryKey = fieldName.equals("id") || fieldName.equals(camelUpperToLower(dataName) + "Id");

        // enum, foreign data class, pojo class, known type (DateTime, Instant, ...)
        boolean customType;
        SqlDataFamily sqlDataFamily;
        if (isPrimitive(fieldType)) {
            customType = false;
            sqlDataFamily = PRIMITIVE_FAMILIES.get(Primitives.unwrap(fieldType));
        } else {
            customType = true;
            sqlDataFamily = SqlDataFamily.Integer;  // ?
        }
        Column column = new Column(camelToSnake(fieldName), new ColumnType(sqlDataFamily, null));

        return new SimpleTableField(field, getter, isPrimaryKey, null, customType, column);
    }

    @VisibleForTesting
    static @Nullable Method findGetterMethod(@NotNull Field field) {
        Class<?> dataClass = field.getDeclaringClass();
        Class<?> fieldType = field.getType();
        String fieldName = field.getName().toLowerCase();
        return Arrays.stream(dataClass.getDeclaredMethods())
                .filter(method -> method.getReturnType() == fieldType &&
                                  method.getParameterCount() == 0 &&
                                  Modifier.isPublic(method.getModifiers()) &&
                                  !Modifier.isStatic(method.getModifiers()) &&
                                  method.getName().toLowerCase().contains(fieldName))
                .findFirst()
                .orElse(null);
    }

    private static final Map<Class<?>, SqlDataFamily> PRIMITIVE_FAMILIES = EasyMaps.asMap(
        boolean.class, SqlDataFamily.Boolean,
        int.class, SqlDataFamily.Integer,
        long.class, SqlDataFamily.Long,
        short.class, SqlDataFamily.Short,
        byte.class, SqlDataFamily.Byte,
        float.class, SqlDataFamily.Float,
        double.class, SqlDataFamily.Double,
        String.class, SqlDataFamily.String,
        byte[].class, SqlDataFamily.Binary,
        Date.class, SqlDataFamily.Date,
        Time.class, SqlDataFamily.Time,
        Timestamp.class, SqlDataFamily.Timestamp
    );

    @SuppressWarnings("ConstantConditions")
    public static @NotNull String camelToSnake(@NotNull String s) {
        return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(s);
    }

    @SuppressWarnings("ConstantConditions")
    public static @NotNull String camelUpperToLower(@NotNull String s) {
        return CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_CAMEL).convert(s);
    }

    public static boolean isPrimitive(@NotNull Class<?> javaType) {
        return javaType.isPrimitive() || Primitives.isWrapperType(javaType) || javaType == String.class;
    }

    public record DataClassInput(@NotNull Class<?> dataClass, @NotNull String dataName) {
        public DataClassInput(@NotNull Class<?> dataClass) {
            this(dataClass, dataClass.getSimpleName());
        }
    }
}
