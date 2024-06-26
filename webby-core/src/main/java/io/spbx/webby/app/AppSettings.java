package io.spbx.webby.app;

import com.google.inject.Inject;
import io.spbx.util.props.MutableLiveProperties;
import io.spbx.util.props.MutablePropertyMap;
import io.spbx.util.props.PropertyMap;
import io.spbx.webby.common.Lifetime;
import io.spbx.webby.routekit.QueryParser;
import io.spbx.webby.routekit.SimpleQueryParser;
import io.spbx.webby.url.annotate.FrameType;
import io.spbx.webby.url.annotate.Marshal;
import io.spbx.webby.url.annotate.Render;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static io.spbx.webby.app.ClassFilterStringConverter.TO_CLASS_FILTER;
import static java.util.Objects.requireNonNull;

public final class AppSettings implements Settings, MutablePropertyMap {
    private static final AtomicReference<PropertyMap> liveRef = new AtomicReference<>();
    private final MutableLiveProperties properties;

    private ClassFilter modelFilter = getOptional(MODEL_FILTER).map(TO_CLASS_FILTER).orElse(ClassFilter.DEFAULT);
    private ClassFilter handlerFilter = getOptional(HANDLER_FILTER).map(TO_CLASS_FILTER).orElse(ClassFilter.DEFAULT);
    private ClassFilter interceptorFilter = getOptional(INTERCEPTOR_FILTER).map(TO_CLASS_FILTER).orElse(ClassFilter.DEFAULT);
    private QueryParser urlParser = SimpleQueryParser.DEFAULT;
    private final StorageSettings storageSettings = new StorageSettings(this);

    private AppSettings(@NotNull MutableLiveProperties properties) {
        this.properties = properties;
        liveRef.set(properties);
    }

    public static @NotNull AppSettings inMemoryForDevOnly() {
        return new AppSettings(MutableLiveProperties.idle(Path.of("%in-memory-for-dev-only%")));
    }

    public static @NotNull AppSettings fromProperties(@NotNull Path path) {
        return new AppSettings(MutableLiveProperties.running(path));
    }

    public static @NotNull AppSettings fromProperties(@NotNull String path) {
        return fromProperties(Path.of(path));
    }

    public static @NotNull AppSettings fromProperties() {
        return fromProperties(DEFAULT_APP_PROPERTIES);
    }

    @Inject
    private void init(@NotNull Lifetime lifetime) {
        lifetime.onTerminate(() -> liveRef.set(null));
        lifetime.onTerminate(properties);
    }

    @CheckReturnValue
    public static @NotNull PropertyMap live() {
        return requireNonNull(liveRef.get(), "LiveProperties are not initialized yet");
    }

    public void setRunMode(@NotNull RunMode runMode) {
        setEnum(RUN_MODE, runMode);
    }

    public void setHotReload(@NotNull Toggle hotReload) {
        setEnum(HOT_RELOAD, hotReload);
    }

    public void setProfileMode(@NotNull Toggle profileMode) {
        setEnum(PROFILE_MODE, profileMode);
    }

    public void setSafeMode(@NotNull Toggle safeMode) {
        setEnum(SAFE_MODE, safeMode);
    }

    public void setCharset(@NotNull Charset charset) {
        setString(CHARSET, charset.toString());
    }

    public void setSecurityKey(@NotNull String securityKey) {
        setString(SECURITY_KEY, securityKey);
    }

    public void setDefaultRender(@NotNull Render defaultRender) {
        setEnum(RENDER.key(), defaultRender);
    }

    public void setWebPath(@NotNull Path webPath) {
        setPath(WEB_PATH, webPath);
    }

    public void setViewPath(@NotNull Path viewPath) {
        setPath(VIEW_PATHS, viewPath);
    }

    public void setStreamingEnabled(boolean streamingEnabled) {
        setBool(HTTP_STREAMING, streamingEnabled);
    }

    public void setDefaultRequestContentMarshal(@NotNull Marshal marshal) {
        setEnum(HTTP_REQUEST_MARSHAL, marshal);
    }

    public void setDefaultResponseContentMarshal(@NotNull Marshal marshal) {
        setEnum(HTTP_RESPONSE_MARSHAL, marshal);
    }

    public void setUserContentPath(@NotNull Path userContentPath) {
        setPath(USER_CONTENT, userContentPath);
    }

    @Override
    public @NotNull ClassFilter modelFilter() {
        return modelFilter;
    }

    public void setModelFilter(@NotNull ClassFilter filter) {
        modelFilter = filter;
    }

    @Override
    public @NotNull ClassFilter handlerFilter() {
        return handlerFilter;
    }

    public void setHandlerFilter(@NotNull ClassFilter filter) {
        handlerFilter = filter;
    }

    @Override
    public @NotNull ClassFilter interceptorFilter() {
        return interceptorFilter;
    }

    public void setInterceptorFilter(@NotNull ClassFilter filter) {
        interceptorFilter = filter;
    }

    @Override
    public @NotNull QueryParser urlParser() {
        return urlParser;
    }

    public void setUrlParser(@NotNull QueryParser urlParser) {
        this.urlParser = urlParser;
    }

    public void setDefaultFrameType(@NotNull FrameType frameType) {
        setEnum(WS_FRAME_TYPE, frameType);
    }

    public void setDefaultFrameContentMarshal(@NotNull Marshal marshal) {
        setEnum(WS_FRAME_MARSHAL, marshal);
    }

    public void setDefaultApiVersion(@NotNull String defaultApiVersion) {
        setString(WS_API_VERSION, defaultApiVersion);
    }

    @Override
    public @NotNull StorageSettings storageSettings() {
        return storageSettings;
    }

    @Override
    public @Nullable String getOrNull(@NotNull String key) {
        return properties.getOrNull(key);
    }

    @Override
    public @Nullable String setString(@NotNull String key, @NotNull String value) {
        return properties.setString(key, value);
    }
}
