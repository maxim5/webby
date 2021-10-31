package io.webby.util.sql.schema;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import io.webby.util.EasyMaps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    Date(java.sql.Date.class),
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

    private static final ImmutableMap<Class<?>, JdbcType> TYPES_BY_CLASS = EasyMaps.mergeToImmutable(
        Arrays.stream(JdbcType.values()).collect(Collectors.toMap(JdbcType::nativeType, type -> type)),
        Map.of(java.util.Date.class, Date)
    );

    public static @Nullable JdbcType findByMatchingNativeType(@NotNull Class<?> nativeType) {
        return TYPES_BY_CLASS.get(Primitives.unwrap(nativeType));
    }
}
