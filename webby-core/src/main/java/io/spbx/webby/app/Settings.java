package io.spbx.webby.app;

import io.spbx.util.props.PropertyMap;
import io.spbx.webby.db.kv.DbType;
import io.spbx.webby.routekit.QueryParser;
import io.spbx.webby.url.annotate.FrameType;
import io.spbx.webby.url.annotate.Marshal;
import io.spbx.webby.url.annotate.Render;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;

public interface Settings extends PropertyMap {
    Path DEFAULT_APP_PROPERTIES = Path.of("app.properties");

    EnumProperty<RunMode> RUN_MODE = EnumProperty.of("webby.mode", RunMode.DEV);
    EnumProperty<Toggle> HOT_RELOAD = EnumProperty.of("webby.debug.hot_reload", Toggle.ENABLED_IN_DEV);
    EnumProperty<Toggle> PROFILE_MODE = EnumProperty.of("webby.debug.profile", Toggle.ENABLED_IN_DEV);
    EnumProperty<Toggle> SAFE_MODE = EnumProperty.of("webby.debug.safe", Toggle.ENABLED_IN_DEV);
    Property CHARSET = Property.of("webby.charset", Charset.defaultCharset().toString());
    Property SECURITY_KEY = Property.of("webby.security.key", "");

    EnumProperty<DbType> KV_TYPE = EnumProperty.of("webby.kv.type", DbType.JAVA_MAP);
    PathProperty KV_STORAGE_PATH = PathProperty.of("webby.kv.path", Path.of("storage"));

    Property SQL_URL = Property.of("webby.sql.url", "");
    Property SQL_USER = Property.of("webby.sql.user", "");
    Property SQL_PASSWORD = Property.of("webby.sql.password", "");

    Property URL_QUERY_PARSER = Property.of("webby.url.query.parser", "");
    EnumProperty<Render> RENDER = EnumProperty.of("webby.render", Render.JTE);
    PathProperty WEB_PATH = PathProperty.of("webby.web.path", Path.of("web"));
    PathProperty VIEW_PATHS = PathProperty.of("webby.view.paths", Path.of("web"));
    BoolProperty HTTP_STREAMING = BoolProperty.of("webby.http.streaming.enabled", false);
    EnumProperty<Marshal> HTTP_REQUEST_MARSHAL = EnumProperty.of("webby.http.request.marshal", Marshal.JSON);
    EnumProperty<Marshal> HTTP_RESPONSE_MARSHAL = EnumProperty.of("webby.http.request.marshal", Marshal.AS_STRING);
    PathProperty USER_CONTENT = PathProperty.of("webby.user.content.path", Path.of("user-content"));

    EnumProperty<FrameType> WS_FRAME_TYPE = EnumProperty.of("webby.ws.frame.type", FrameType.FROM_CLIENT);
    EnumProperty<Marshal> WS_FRAME_MARSHAL = EnumProperty.of("webby.ws.frame.marshal", Marshal.JSON);
    Property WS_API_VERSION = Property.of("webby.ws.api.version", "1.0");

    Property MODEL_FILTER = Property.of("webby.classpath.model.filter", "default");
    Property HANDLER_FILTER = Property.of("webby.classpath.handler.filter", "default");
    Property INTERCEPTOR_FILTER = Property.of("webby.classpath.interceptor.filter", "default");

    IntProperty SIZE_BYTES = IntProperty.of("webby.byte.stream.size", 1024);
    IntProperty SIZE_CHARS = IntProperty.of("webby.char.stream.size", 1024);
    IntProperty SQL_MAX_PARAMS = IntProperty.of("webby.sql.max.params", 1024);

    default @NotNull RunMode runMode() { return getEnum(RUN_MODE); }
    default boolean isDevMode() { return runMode().isDev(); }
    default boolean isProdMode() { return runMode().isProd(); }

    default @NotNull Toggle hotReload() { return getEnum(HOT_RELOAD); }
    default boolean isHotReload() { return hotReload().isEnabledFor(runMode()); }

    default @NotNull Toggle profileMode() { return getEnum(PROFILE_MODE); }
    default boolean isProfileMode() { return profileMode().isEnabledFor(runMode()); }

    default @NotNull Toggle safeMode() { return getEnum(SAFE_MODE); }
    default boolean isSafeMode() { return safeMode().isEnabledFor(runMode()); }

    default @NotNull Charset charset() { return Charset.forName(get(CHARSET)); }
    default byte @NotNull [] securityKey() { return get(SECURITY_KEY).getBytes(charset()); }

    default @NotNull Render defaultRender() { return getEnum(RENDER); }
    default @NotNull Path webPath() { return getPath(WEB_PATH); }
    default @NotNull List<Path> viewPaths() { return getPaths(VIEW_PATHS); }
    default @NotNull List<Path> viewPaths(@NotNull String key) { return getOptionalPaths(key).orElseGet(this::viewPaths); }
    default boolean isStreamingEnabled() { return getBool(HTTP_STREAMING); }
    default @NotNull Marshal defaultRequestContentMarshal() { return getEnum(HTTP_REQUEST_MARSHAL); }
    default @NotNull Marshal defaultResponseContentMarshal() { return getEnum(HTTP_RESPONSE_MARSHAL); }
    default @NotNull Path userContentPath() { return getPath(USER_CONTENT); }

    default @NotNull FrameType defaultFrameType() { return getEnum(WS_FRAME_TYPE); }
    default @NotNull Marshal defaultFrameContentMarshal() { return getEnum(WS_FRAME_MARSHAL); }
    default @NotNull String defaultApiVersion() { return get(WS_API_VERSION); }

    @NotNull ClassFilter modelFilter();
    @NotNull ClassFilter handlerFilter();
    @NotNull ClassFilter interceptorFilter();

    @NotNull QueryParser urlParser();
    @NotNull StorageSettings storageSettings();

    enum RunMode {
        DEV,
        PROD;

        public boolean isDev() { return this == DEV; }
        public boolean isProd() { return this == PROD; }
    }

    enum Toggle {
        ENABLED,
        DISABLED,
        ENABLED_IN_DEV;

        public static @NotNull Toggle from(boolean enabled) {
            return enabled ? ENABLED : DISABLED;
        }
        public boolean isEnabledFor(@NotNull RunMode runMode) {
            return this == ENABLED || (this == ENABLED_IN_DEV && runMode.isDev());
        }
    }
}
