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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.requireNonNull;

public final class AppSettings implements Settings, MutablePropertyMap {
    private boolean devMode = true;
    private boolean hotReload = true;
    private boolean hotReloadDefault = true;
    private boolean profileMode = true;
    private boolean profileModeDefault = true;
    private boolean safeMode = true;
    private boolean safeModeDefault = true;
    private boolean streamingEnabled = false;

    private byte[] securityKey = new byte[0];

    private Path webPath = Path.of("web");
    private List<Path> viewPaths = List.of(Path.of("web"));
    private Path userContentPath = Path.of("usercontent");

    private Charset charset = Charset.defaultCharset();

    private final ClassFilter modelFilter = ClassFilter.empty();
    private final ClassFilter handlerFilter = ClassFilter.empty();
    private final ClassFilter interceptorFilter = ClassFilter.empty();

    private QueryParser urlParser = SimpleQueryParser.DEFAULT;

    private String defaultApiVersion = "1.0";
    private Marshal defaultRequestContentMarshal = Marshal.JSON;
    private Marshal defaultResponseContentMarshal = Marshal.AS_STRING;
    private Marshal defaultFrameContentMarshal = Marshal.JSON;
    private Render defaultRender = Render.JTE;
    private FrameType defaultFrameType = FrameType.FROM_CLIENT;

    private final StorageSettings storageSettings = new StorageSettings(this);

    private static final AtomicReference<PropertyMap> liveRef = new AtomicReference<>();
    private final MutableLiveProperties properties;

    private AppSettings(@NotNull MutableLiveProperties properties) {
        this.properties = properties;
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
        liveRef.set(properties);
        lifetime.onTerminate(() -> liveRef.set(null));
        lifetime.onTerminate(properties);
    }

    @CheckReturnValue
    public static @NotNull PropertyMap live() {
        return requireNonNull(liveRef.get(), "LiveProperties are not initialized yet");
    }

    @Override
    public boolean isDevMode() {
        return devMode;
    }

    @Override
    public boolean isProdMode() {
        return !devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    @Override
    public boolean isHotReload() {
        return hotReload;
    }

    public boolean isHotReloadDefault() {
        return hotReloadDefault;
    }

    public void setHotReload(boolean hotReload) {
        this.hotReload = hotReload;
        this.hotReloadDefault = false;
    }

    @Override
    public boolean isProfileMode() {
        return profileMode;
    }

    public void setProfileMode(boolean profileMode) {
        this.profileMode = profileMode;
        this.profileModeDefault = false;
    }

    public boolean isProfileModeDefault() {
        return profileModeDefault;
    }

    @Override
    public boolean isSafeMode() {
        return safeMode;
    }

    public boolean isSafeModeDefault() {
        return safeModeDefault;
    }

    public void setSafeMode(boolean safeMode) {
        this.safeMode = safeMode;
        this.safeModeDefault = false;
    }

    @Override
    public boolean isStreamingEnabled() {
        return streamingEnabled;
    }

    public void setStreamingEnabled(boolean streamingEnabled) {
        this.streamingEnabled = streamingEnabled;
    }

    @Override
    public byte @NotNull [] securityKey() {
        return securityKey;
    }

    public void setSecurityKey(@NotNull String securityKey) {
        this.securityKey = securityKey.getBytes(charset);
    }

    public void setSecurityKey(byte[] securityKey) {
        this.securityKey = securityKey;
    }

    @Override
    public @NotNull Path webPath() {
        return webPath;
    }

    public void setWebPath(@NotNull Path webPath) {
        this.webPath = webPath;
    }

    public void setWebPath(@NotNull String webPath) {
        this.webPath = Path.of(webPath);
    }

    @Override
    public @NotNull List<Path> viewPaths() {
        return viewPaths;
    }

    public void setViewPath(@NotNull String viewPath) {
        this.viewPaths = List.of(Path.of(viewPath));
    }

    public void setViewPaths(@NotNull String @NotNull ... viewPaths) {
        this.viewPaths = Arrays.stream(viewPaths).map(Path::of).toList();
    }

    public void setViewPath(@NotNull Path viewPath) {
        this.viewPaths = List.of(viewPath);
    }

    public void setViewPaths(@NotNull Path @NotNull ... viewPaths) {
        this.viewPaths = List.of(viewPaths);
    }

    @Override
    public @NotNull Path userContentPath() {
        return userContentPath;
    }

    public void setUserContentPath(@NotNull Path userContentPath) {
        this.userContentPath = userContentPath;
    }

    public void setUserContentPath(@NotNull String userContentPath) {
        this.userContentPath = Path.of(userContentPath);
    }

    @Override
    public @NotNull Charset charset() {
        return charset;
    }

    public void setCharset(@NotNull Charset charset) {
        this.charset = charset;
    }

    @Override
    public @NotNull ClassFilter modelFilter() {
        return modelFilter;
    }

    @Override
    public @NotNull ClassFilter handlerFilter() {
        return handlerFilter;
    }

    @Override
    public @NotNull ClassFilter interceptorFilter() {
        return interceptorFilter;
    }

    @Override
    public @NotNull QueryParser urlParser() {
        return urlParser;
    }

    public void setUrlParser(@NotNull QueryParser urlParser) {
        this.urlParser = urlParser;
    }

    @Override
    public @NotNull String defaultApiVersion() {
        return defaultApiVersion;
    }

    public void setDefaultApiVersion(@NotNull String defaultApiVersion) {
        this.defaultApiVersion = defaultApiVersion;
    }

    @Override
    public @NotNull Marshal defaultRequestContentMarshal() {
        return defaultRequestContentMarshal;
    }

    public void setDefaultRequestContentMarshal(@NotNull Marshal marshal) {
        this.defaultRequestContentMarshal = marshal;
    }

    @Override
    public @NotNull Marshal defaultResponseContentMarshal() {
        return defaultResponseContentMarshal;
    }

    public void setDefaultResponseContentMarshal(@NotNull Marshal marshal) {
        this.defaultResponseContentMarshal = marshal;
    }

    @Override
    public @NotNull Marshal defaultFrameContentMarshal() {
        return defaultFrameContentMarshal;
    }

    public void setDefaultFrameContentMarshal(@NotNull Marshal marshal) {
        this.defaultFrameContentMarshal = marshal;
    }

    @Override
    public @NotNull Render defaultRender() {
        return defaultRender;
    }

    public void setDefaultRender(@NotNull Render defaultRender) {
        this.defaultRender = defaultRender;
    }

    @Override
    public @NotNull FrameType defaultFrameType() {
        return defaultFrameType;
    }

    public void setDefaultFrameType(@NotNull FrameType frameType) {
        this.defaultFrameType = frameType;
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
    public @Nullable String setProperty(@NotNull String key, @NotNull String value) {
        return properties.setProperty(key, value);
    }
}
