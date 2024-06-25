package io.spbx.util.props;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CanIgnoreReturnValue
public interface MutablePropertyMap extends PropertyMap {
    @Nullable String setProperty(@NotNull String key, @NotNull String value);

    default @Nullable String setProperty(@NotNull String key, long value) {
        return setProperty(key, Long.toString(value));
    }

    default @Nullable String setProperty(@NotNull String key, int value) {
        return setProperty(key, Integer.toString(value));
    }

    default @Nullable String setProperty(@NotNull String key, boolean value) {
        return setProperty(key, Boolean.toString(value));
    }

    default <T extends Enum<T>> @Nullable String setProperty(@NotNull String key, @NotNull T value) {
        return setProperty(key, value.name());
    }

    default @Nullable String setProperty(@NotNull String key, @NotNull Object value) {
        return setProperty(key, value.toString());
    }
}
