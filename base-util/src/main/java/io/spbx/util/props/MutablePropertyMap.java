package io.spbx.util.props;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

@CanIgnoreReturnValue
public interface MutablePropertyMap extends PropertyMap {
    @Nullable String setString(@NotNull String key, @NotNull String val);
    default @Nullable String setString(@NotNull Property prop, @NotNull String val) { return setString(prop.key(), val); }

    default @Nullable String setInt(@NotNull String key, int val) { return setString(key, Integer.toString(val)); }
    default @Nullable String setInt(@NotNull IntProperty prop, int val) { return setInt(prop.key(), val); }

    default @Nullable String setLong(@NotNull String key, long val) { return setString(key, Long.toString(val)); }
    default @Nullable String setLong(@NotNull LongProperty prop, long val) { return setLong(prop.key(), val); }

    default @Nullable String setByte(@NotNull String key, byte val) { return setString(key, Byte.toString(val)); }
    default @Nullable String setByte(@NotNull ByteProperty prop, byte val) { return setByte(prop.key(), val); }

    default @Nullable String setBool(@NotNull String key, boolean val) { return setString(key, Boolean.toString(val)); }
    default @Nullable String setBool(@NotNull BoolProperty prop, boolean val) { return setBool(prop.key(), val); }

    default @Nullable String setDouble(@NotNull String key, double val) { return setString(key, Double.toString(val)); }
    default @Nullable String setDouble(@NotNull DoubleProperty prop, double val) { return setDouble(prop.key(), val); }

    default @Nullable String setFloat(@NotNull String key, float val) { return setString(key, Float.toString(val)); }
    default @Nullable String setFloat(@NotNull FloatProperty prop, float val) { return setFloat(prop.key(), val); }

    default @Nullable String setPath(@NotNull String key, @NotNull Path val) { return setString(key, val.toString()); }
    default @Nullable String setPath(@NotNull PathProperty prop, @NotNull Path val) { return setPath(prop.key(), val); }

    default <T extends Enum<T>> @Nullable String setEnum(@NotNull String key, @NotNull T val) {
        return setString(key, val.name());
    }
    default <T extends Enum<T>> @Nullable String setEnum(@NotNull EnumProperty<T> prop, @NotNull T val) {
        return setEnum(prop.key(), val);
    }
}
