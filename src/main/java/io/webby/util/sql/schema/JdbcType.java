package io.webby.util.sql.schema;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum JdbcType {
    Boolean(boolean.class),
    Int(int.class),
    Long(long.class),
    Short(short.class),
    Byte(byte.class),
    Float(float.class),
    Double(double.class),
    String(String.class),
    Bytes(byte[].class),
    Date(java.util.Date.class),
    Time(java.sql.Time.class),
    Timestamp(java.sql.Timestamp.class);

    private final Class<?> nativeType;

    JdbcType(@NotNull Class<?> nativeType) {
        this.nativeType = nativeType;
    }

    public @NotNull Class<?> nativeType() {
        return nativeType;
    }

    public @NotNull String getterMethod() {
        return "get%s".formatted(name());
    }

    public static final Map<Class<?>, JdbcType> TYPES_BY_CLASS =
            Arrays.stream(JdbcType.values()).collect(Collectors.toMap(JdbcType::nativeType, type -> type));
}
