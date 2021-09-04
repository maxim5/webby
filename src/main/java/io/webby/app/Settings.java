package io.webby.app;

import io.routekit.QueryParser;
import io.webby.db.kv.StorageType;
import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import io.webby.url.annotate.Render;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

public interface Settings {
    boolean isDevMode();

    boolean isProdMode();

    boolean isHotReload();

    boolean isProfileMode();

    boolean isSafeMode();

    boolean isStreamingEnabled();

    byte @NotNull [] securityKey();

    @NotNull Path webPath();

    @NotNull List<Path> viewPaths();

    @NotNull Path storagePath();

    @NotNull Charset charset();

    @NotNull BiPredicate<String, String> handlerFilter();

    @NotNull BiPredicate<String, String> interceptorFilter();

    @NotNull QueryParser urlParser();

    @NotNull String defaultApiVersion();

    @NotNull Marshal defaultRequestContentMarshal();

    @NotNull Marshal defaultResponseContentMarshal();

    @NotNull Marshal defaultFrameContentMarshal();

    @NotNull Render defaultRender();

    @NotNull FrameType defaultFrameType();

    @NotNull StorageType storageType();

    @Nullable String getProperty(@NotNull String key);

    @NotNull String getProperty(@NotNull String key, @NotNull String def);

    @NotNull String getProperty(@NotNull String key, @NotNull Object def);

    default @NotNull Optional<String> getOptionalProperty(@NotNull String key) {
        return Optional.ofNullable(getProperty(key));
    }

    int getIntProperty(@NotNull String key, int def);

    long getLongProperty(@NotNull String key, long def);

    byte getByteProperty(@NotNull String key, int def);

    boolean getBoolProperty(@NotNull String key);

    boolean getBoolProperty(@NotNull String key, boolean def);

    <T extends Enum<T>> @NotNull T getEnumProperty(@NotNull String key, @NotNull T def);

    @NotNull List<Path> getViewPaths(@NotNull String key);
}
