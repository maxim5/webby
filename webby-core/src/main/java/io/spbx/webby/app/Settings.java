package io.spbx.webby.app;

import io.spbx.util.props.PropertyMap;
import io.spbx.webby.routekit.QueryParser;
import io.spbx.webby.url.annotate.FrameType;
import io.spbx.webby.url.annotate.Marshal;
import io.spbx.webby.url.annotate.Render;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

public interface Settings extends SettingsFilters, PropertyMap {
    boolean isDevMode();

    boolean isProdMode();

    boolean isHotReload();

    boolean isProfileMode();

    boolean isSafeMode();

    boolean isStreamingEnabled();

    byte @NotNull [] securityKey();

    @NotNull Path webPath();

    @NotNull List<Path> viewPaths();

    default @NotNull List<Path> getViewPaths(@NotNull String key) {
        return getOptional(key).map(PropertyMap::toPaths).orElseGet(this::viewPaths);
    }

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

    Path DEFAULT_APP_PROPERTIES = Path.of("app.properties");
    IntProperty SIZE_BYTES = IntProperty.of("webby.byte.stream.size", 1024);
    IntProperty SIZE_CHARS = IntProperty.of("webby.char.stream.size", 1024);
    IntProperty SQL_MAX_PARAMS = IntProperty.of("webby.sql.max.params", 1024);
}
