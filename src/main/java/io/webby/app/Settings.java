package io.webby.app;

import io.routekit.QueryParser;
import io.webby.url.annotate.Marshal;
import io.webby.url.annotate.Render;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiPredicate;

public interface Settings {
    boolean isDevMode();

    boolean isHotReload();

    boolean isSafeMode();

    byte[] securityKey();

    @NotNull Path webPath();

    @NotNull List<Path> viewPaths();

    @NotNull Charset charset();

    @NotNull BiPredicate<String, String> handlerFilter();

    @NotNull BiPredicate<String, String> interceptorFilter();

    @NotNull QueryParser urlParser();

    @NotNull Marshal defaultRequestContentMarshal();

    @NotNull Marshal defaultResponseContentMarshal();

    @NotNull Render defaultRender();

    @Nullable String getProperty(@NotNull String key);

    @NotNull String getProperty(@NotNull String key, @NotNull String def);

    @NotNull String getProperty(@NotNull String key, @NotNull Object def);

    int getIntProperty(@NotNull String key, int def);

    boolean getBoolProperty(@NotNull String key);

    boolean getBoolProperty(@NotNull String key, boolean def);

    @NotNull List<Path> getViewPaths(@NotNull String key);
}
