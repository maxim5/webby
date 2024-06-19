package io.spbx.webby.app;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface SettingsProps {
    @Nullable String getProperty(@NotNull String key);

    @NotNull String getProperty(@NotNull String key, @NotNull String def);

    @NotNull String getProperty(@NotNull String key, @NotNull Object def);

    default @NotNull Optional<String> getOptionalProperty(@NotNull String key) {
        return Optional.ofNullable(getProperty(key));
    }

    int getIntProperty(@NotNull String key, int def);

    long getLongProperty(@NotNull String key, long def);

    byte getByteProperty(@NotNull String key, int def);

    float getFloatProperty(@NotNull String key, float def);

    double getDoubleProperty(@NotNull String key, double def);

    boolean getBoolProperty(@NotNull String key);

    boolean getBoolProperty(@NotNull String key, boolean def);

    <T extends Enum<T>> @NotNull T getEnumProperty(@NotNull String key, @NotNull T def);

    @NotNull List<Path> getViewPaths(@NotNull String key);
}
