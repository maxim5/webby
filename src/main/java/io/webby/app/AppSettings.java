package io.webby.app;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.routekit.QueryParser;
import io.routekit.SimpleQueryParser;
import io.webby.common.Packages;
import io.webby.db.kv.StorageType;
import io.webby.url.annotate.FrameType;
import io.webby.url.annotate.Marshal;
import io.webby.url.annotate.Render;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiPredicate;

import static io.webby.util.EasyCast.castAny;

public final class AppSettings implements Settings {
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
    private Path storagePath = Path.of("storage");
    private Charset charset = Charset.defaultCharset();

    private final BiPredicate<String, String> excludeImplFilter =
            (pkg, __) -> !pkg.startsWith(Packages.RENDER_IMPL) && !pkg.startsWith(Packages.DB_IMPL);
    private BiPredicate<String, String> handlerFilter = null;
    private BiPredicate<String, String> interceptorFilter = null;

    private QueryParser urlParser = SimpleQueryParser.DEFAULT;

    private String defaultApiVersion = "1.0";
    private Marshal defaultRequestContentMarshal = Marshal.JSON;
    private Marshal defaultResponseContentMarshal = Marshal.AS_STRING;
    private Marshal defaultFrameContentMarshal = Marshal.JSON;
    private Render defaultRender = Render.JTE;
    private FrameType defaultFrameType = FrameType.FROM_CLIENT;

    private StorageType storageType = StorageType.MAP_DB;

    private final Map<String, String> properties = new HashMap<>();

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
    public @NotNull Path storagePath() {
        return storagePath;
    }

    public void setStoragePath(@NotNull Path storagePath) {
        this.storagePath = storagePath;
    }

    public void setStoragePath(@NotNull String storagePath) {
        this.storagePath = Path.of(storagePath);
    }

    @Override
    public @NotNull Charset charset() {
        return charset;
    }

    public void setCharset(@NotNull Charset charset) {
        this.charset = charset;
    }

    @Override
    public @NotNull BiPredicate<String, String> handlerFilter() {
        return handlerFilter;
    }

    public boolean isHandlerFilterSet() {
        return handlerFilter != null;
    }

    public void setHandlerPackageOnly(@NotNull String packageName) {
        this.handlerFilter = allInPackage(packageName).and(excludeImplFilter);
    }

    public void setHandlerClassOnly(@NotNull Class<?> klass) {
        this.handlerFilter = onlyClass(klass).and(excludeImplFilter);
    }

    public void setHandlerFilter(@NotNull BiPredicate<String, String> filter) {
        this.handlerFilter = filter;
    }

    @Override
    public @NotNull BiPredicate<String, String> interceptorFilter() {
        return interceptorFilter;
    }

    public boolean isInterceptorFilterSet() {
        return interceptorFilter != null;
    }

    public void setInterceptorPackageOnly(@NotNull String packageName) {
        this.interceptorFilter = allInPackage(packageName).and(excludeImplFilter);
    }

    public void setInterceptorClassOnly(@NotNull Class<?> klass) {
        this.interceptorFilter = onlyClass(klass).and(excludeImplFilter);
    }

    public void setInterceptorFilter(@NotNull BiPredicate<String, String> filter) {
        this.interceptorFilter = filter;
    }

    @NotNull
    private static BiPredicate<String, String> onlyClass(@NotNull Class<?> klass) {
        return (pkg, cls) -> pkg.equals(klass.getPackageName()) && cls.equals(klass.getSimpleName());
    }

    @NotNull
    private static BiPredicate<String, String> allInPackage(@NotNull String packageName) {
        return (pkg, cls) -> pkg.startsWith(packageName);
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
    public @NotNull StorageType storageType() {
        return storageType;
    }

    public void setStorageType(@NotNull StorageType storageType) {
        this.storageType = storageType;
    }

    @CanIgnoreReturnValue
    @Nullable
    public String setProperty(@NotNull String key, @NotNull String value) {
        return properties.put(key, value);
    }

    @CanIgnoreReturnValue
    @Nullable
    public String setProperty(@NotNull String key, long value) {
        return properties.put(key, Long.toString(value));
    }

    @CanIgnoreReturnValue
    @Nullable
    public String setProperty(@NotNull String key, int value) {
        return properties.put(key, Integer.toString(value));
    }

    @CanIgnoreReturnValue
    @Nullable
    public String setProperty(@NotNull String key, boolean value) {
        return properties.put(key, Boolean.toString(value));
    }

    @CanIgnoreReturnValue
    public <T extends Enum<T>> @Nullable String setProperty(@NotNull String key, @NotNull T value) {
        return properties.put(key, value.name());
    }

    @CanIgnoreReturnValue
    public @Nullable String setProperty(@NotNull String key, @NotNull Object value) {
        return properties.put(key, value.toString());
    }

    @Override
    @Nullable
    public String getProperty(@NotNull String key) {
        return properties.get(key);
    }

    @Override
    @NotNull
    public String getProperty(@NotNull String key, @NotNull String def) {
        return properties.getOrDefault(key, def);
    }

    @Override
    @NotNull
    public String getProperty(@NotNull String key, @NotNull Object def) {
        String property = properties.get(key);
        return property != null ? property : def.toString();
    }

    @Override
    public int getIntProperty(@NotNull String key, int def) {
        try {
            String property = getProperty(key);
            return property != null ? Integer.parseInt(property) : def;
        } catch (NumberFormatException ignore) {
            return def;
        }
    }

    @Override
    public long getLongProperty(@NotNull String key, long def) {
        try {
            String property = getProperty(key);
            return property != null ? Long.parseLong(property) : def;
        } catch (NumberFormatException ignore) {
            return def;
        }
    }

    @Override
    public byte getByteProperty(@NotNull String key, int def) {
        return (byte) getIntProperty(key, def);
    }

    @Override
    public boolean getBoolProperty(@NotNull String key) {
        return getBoolProperty(key, false);
    }

    @Override
    public boolean getBoolProperty(@NotNull String key, boolean def) {
        String property = getProperty(key);
        return property != null ? Boolean.parseBoolean(property) : def;
    }

    @Override
    public <T extends Enum<T>> @NotNull T getEnumProperty(@NotNull String key, @NotNull T def) {
        String property = getProperty(key);
        if (property == null) {
            return def;
        }

        Class<T> enumClass = castAny(def.getClass());
        T[] enumConstants = enumClass.getEnumConstants();
        Optional<T> exactMatch = Arrays.stream(enumConstants).filter(v -> v.name().equals(key)).findFirst();
        if (exactMatch.isPresent()) {
            return exactMatch.get();
        }
        Optional<T> closeMatch = Arrays.stream(enumConstants).filter(v -> v.name().equalsIgnoreCase(key)).findFirst();
        return closeMatch.orElse(def);
    }

    @Override
    public @NotNull List<Path> getViewPaths(@NotNull String key) {
        String property = getProperty(key);
        return property != null ?
                Arrays.stream(property.split(File.pathSeparator)).map(Path::of).toList() :
                viewPaths();
    }
}
