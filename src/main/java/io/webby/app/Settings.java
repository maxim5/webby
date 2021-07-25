package io.webby.app;

import io.routekit.QueryParser;
import io.webby.url.annotate.Marshal;
import io.webby.url.annotate.Render;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiPredicate;

public interface Settings {
    boolean isDevMode();

    boolean isHotReload();

    Path webPath();

    List<Path> viewPaths();

    BiPredicate<String, String> filter();

    QueryParser urlParser();

    Marshal defaultRequestContentMarshal();

    Marshal defaultResponseContentMarshal();

    Render defaultRender();

    @Nullable String getProperty(@NotNull String key);

    @NotNull String getProperty(@NotNull String key, @NotNull String def);

    @NotNull String getProperty(@NotNull String key, @NotNull Object def);

    int getIntProperty(@NotNull String key, int def);
}
