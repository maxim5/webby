package io.spbx.util.props;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.spbx.util.base.EasyCast.castAny;
import static io.spbx.util.base.EasyNulls.firstNonNull;
import static io.spbx.util.base.EasyNulls.firstNonNullIfExist;
import static io.spbx.util.base.EasyPrimitives.*;
import static java.util.Objects.requireNonNull;

@CheckReturnValue
public interface PropertyMap {
    static @NotNull PropertyMap system() {
        return System::getProperty;
    }

    @Nullable String getOrNull(@NotNull String key);
    default @Nullable String getOrNull(@NotNull Property property) { return getOrNull(property.key()); }
    default @Nullable String getOrDie(@NotNull String key) { return requireNonNull(getOrNull(key)); }
    default @Nullable String getOrDie(@NotNull Property property) { return getOrDie(property.key()); }
    default @NotNull Optional<String> getOptional(@NotNull String key) { return Optional.ofNullable(getOrNull(key)); }
    default @NotNull Optional<String> getOptional(@NotNull Property property) { return getOptional(property.key()); }
    default @NotNull String get(@NotNull String key, @NotNull String def) { return firstNonNull(getOrNull(key), def); }
    default @NotNull String get(@NotNull Property property) { return get(property.key(), property.def()); }

    default int getIntOrNull(@NotNull String key) { return getInt(key, 0); }
    default int getIntOrDie(@NotNull String key) { return parseIntSafe(getOrDie(key)); }
    default int getInt(@NotNull String key, int def) { return parseIntSafe(getOrNull(key), def); }
    default int getInt(@NotNull IntProperty property) { return getInt(property.key(), property.def()); }

    default long getLongOrNull(@NotNull String key) { return getLong(key, 0L); }
    default long getLongOrDie(@NotNull String key) { return parseLongSafe(getOrDie(key)); }
    default long getLong(@NotNull String key, long def) { return parseLongSafe(getOrNull(key), def); }
    default long getLong(@NotNull LongProperty property) { return getLong(property.key(), property.def()); }

    default byte getByteOrNull(@NotNull String key) { return getByte(key, (byte) 0); }
    default byte getByteOrDie(@NotNull String key) { return parseByteSafe(getOrDie(key)); }
    default byte getByte(@NotNull String key, byte def) { return parseByteSafe(getOrNull(key), def); }
    default byte getByte(@NotNull ByteProperty property) { return getByte(property.key(), property.def()); }

    default boolean getBoolOrFalse(@NotNull String key) { return getBool(key, false); }
    default boolean getBoolOrTrue(@NotNull String key) { return getBool(key, true); }
    default boolean getBoolOrDie(@NotNull String key) { return parseBoolSafe(getOrDie(key)); }
    default boolean getBool(@NotNull String key, boolean def) { return parseBoolSafe(getOrNull(key), def); }
    default boolean getBool(@NotNull BoolProperty property) { return getBool(property.key(), property.def()); }

    default double getDoubleOrNull(@NotNull String key) { return getDouble(key, 0.0); }
    default double getDoubleOrDie(@NotNull String key) { return parseDoubleSafe(getOrDie(key)); }
    default double getDouble(@NotNull String key, double def) { return parseDoubleSafe(getOrNull(key), def); }
    default double getDouble(@NotNull DoubleProperty property) { return getDouble(property.key(), property.def()); }

    default float getFloatOrNull(@NotNull String key) { return getFloat(key, 0.0f); }
    default float getFloatOrDie(@NotNull String key) { return parseFloatSafe(getOrDie(key)); }
    default float getFloat(@NotNull String key, float def) { return parseFloatSafe(getOrNull(key), def); }
    default float getFloat(@NotNull FloatProperty property) { return getFloat(property.key(), property.def()); }

    default @Nullable Path getPathOrNull(@NotNull String key) { return getOptionalPath(key).orElse(null); }
    default @Nullable Path getPathOrDie(@NotNull String key) { return requireNonNull(getPathOrNull(key)); }
    default @NotNull Optional<Path> getOptionalPath(@NotNull String key) { return getOptional(key).map(Path::of); }
    default @NotNull Optional<Path> getOptionalPath(@NotNull PathProperty prop) { return getOptionalPath(prop.key()); }
    default @NotNull Path getPath(@NotNull String key, @NotNull Path def) { return firstNonNull(getPathOrNull(key), def); }
    default @NotNull Path getPath(@NotNull PathProperty property) { return getPath(property.key(), property.def()); }

    default @NotNull List<Path> getPaths(@NotNull String key) { return getPaths(key, List.of()); }
    default @NotNull List<Path> getPaths(@NotNull PathProperty property) {
        return getPaths(property.key(), List.of(property.def()));
    }
    default @NotNull List<Path> getPaths(@NotNull String key, @NotNull List<Path> def) {
        return getOptionalPaths(key).orElse(def);
    }
    default @NotNull Optional<List<Path>> getOptionalPaths(@NotNull String key) {
        return getOptional(key).map(PropertyMap::toPaths);
    }

    default <T extends Enum<T>> @NotNull T getEnum(@NotNull String key, @NotNull T def) {
        return getOptional(key).map(property -> toEnum(property, def)).orElse(def);
    }
    default <T extends Enum<T>> @NotNull T getEnum(@NotNull EnumProperty<T> property) {
        return getEnum(property.key(), property.def());
    }

    default @NotNull PropertyMap chainedWith(@NotNull PropertyMap backup) {
        return key -> firstNonNullIfExist(this.getOrNull(key), () -> backup.getOrNull(key));
    }

    record Property(@NotNull String key, @NotNull String def) {
        public static @NotNull Property of(@NotNull String key, @NotNull String def) { return new Property(key, def); }
        public static @NotNull Property of(@NotNull String key) { return of(key, ""); }
    }

    record IntProperty(@NotNull String key, int def) {
        public static @NotNull IntProperty of(@NotNull String key, int def) { return new IntProperty(key, def); }
        public static @NotNull IntProperty of(@NotNull String key) { return of(key, 0); }
    }

    record LongProperty(@NotNull String key, long def) {
        public static @NotNull LongProperty of(@NotNull String key, long def) { return new LongProperty(key, def); }
        public static @NotNull LongProperty of(@NotNull String key) { return of(key, 0); }
    }

    record ByteProperty(@NotNull String key, byte def) {
        public static @NotNull ByteProperty of(@NotNull String key, byte def) { return new ByteProperty(key, def); }
        public static @NotNull ByteProperty of(@NotNull String key) { return of(key, (byte) 0); }
    }

    record BoolProperty(@NotNull String key, boolean def) {
        public static @NotNull BoolProperty of(@NotNull String key, boolean def) { return new BoolProperty(key, def); }
        public static @NotNull BoolProperty of(@NotNull String key) { return of(key, false); }
    }

    record DoubleProperty(@NotNull String key, double def) {
        public static @NotNull DoubleProperty of(@NotNull String key, double def) { return new DoubleProperty(key, def); }
        public static @NotNull DoubleProperty of(@NotNull String key) { return of(key, 0.0); }
    }

    record FloatProperty(@NotNull String key, float def) {
        public static @NotNull FloatProperty of(@NotNull String key, float def) { return new FloatProperty(key, def); }
        public static @NotNull FloatProperty of(@NotNull String key) { return of(key, 0.0f); }
    }

    record PathProperty(@NotNull String key, @NotNull Path def) {
        public static @NotNull PathProperty of(@NotNull String key, @NotNull Path def) {
            return new PathProperty(key, def);
        }
    }

    record EnumProperty<T extends Enum<T>>(@NotNull String key, @NotNull T def) {
        public static <T extends Enum<T>> @NotNull EnumProperty<T> of(@NotNull String key, @NotNull T def) {
            return new EnumProperty<>(key, def);
        }
    }

    private static @NotNull List<Path> toPaths(@NotNull String property) {
        return Arrays.stream(property.split(File.pathSeparator)).map(Path::of).toList();
    }

    private static <T extends Enum<T>> @NotNull T toEnum(@NotNull String property, @NotNull T def) {
        Class<T> enumClass = castAny(def.getClass());
        T[] enumConstants = enumClass.getEnumConstants();
        Optional<T> exactMatch = Arrays.stream(enumConstants).filter(v -> v.name().equals(property)).findFirst();
        if (exactMatch.isPresent()) {
            return exactMatch.get();
        }
        Optional<T> closeMatch = Arrays.stream(enumConstants).filter(v -> v.name().equalsIgnoreCase(property)).findFirst();
        return closeMatch.orElse(def);
    }
}
