package io.webby.app;

import io.routekit.QueryParser;
import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import io.webby.url.annotate.Render;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

public interface Settings extends SettingsFilters, SettingsProps {
    boolean isDevMode();

    boolean isProdMode();

    boolean isHotReload();

    boolean isProfileMode();

    boolean isSafeMode();

    boolean isStreamingEnabled();

    byte @NotNull [] securityKey();

    @NotNull Path webPath();

    @NotNull List<Path> viewPaths();

    @NotNull Path userContentPath();

    @NotNull Charset charset();

    @NotNull QueryParser urlParser();

    @NotNull String defaultApiVersion();

    @NotNull Marshal defaultRequestContentMarshal();

    @NotNull Marshal defaultResponseContentMarshal();

    @NotNull Marshal defaultFrameContentMarshal();

    @NotNull Render defaultRender();

    @NotNull FrameType defaultFrameType();

    @NotNull StorageSettings storageSettings();
}
