package io.webby.app;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.routekit.QueryParser;
import io.routekit.SimpleQueryParser;
import io.webby.db.kv.StorageType;
import io.webby.url.annotate.Marshal;
import io.webby.url.annotate.Render;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public final class AppSettings implements Settings {
    private boolean devMode = true;
    private boolean hotReload = true;
    private boolean hotReloadDefault = true;
    private boolean safeMode = true;
    private boolean safeModeDefault = true;

    private byte[] securityKey = new byte[0];

    private Path webPath = Path.of("/web/");
    private List<Path> viewPaths = List.of(Path.of("/web/"));
    private Charset charset = Charset.defaultCharset();

    private BiPredicate<String, String> handlerFilter = null;
    private BiPredicate<String, String> interceptorFilter = null;

    private QueryParser urlParser = SimpleQueryParser.DEFAULT;

    private Marshal defaultRequestContentMarshal = Marshal.JSON;
    private Marshal defaultResponseContentMarshal = Marshal.AS_STRING;
    private Render defaultRender = Render.JTE;

    private StorageType storageType = StorageType.MAP_DB;

    private final Map<String, String> properties = new HashMap<>();

    @Override
    public boolean isDevMode() {
        return devMode;
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
    public byte[] securityKey() {
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
        this.handlerFilter = (pkg, cls) -> pkg.startsWith(packageName);
    }

    public void setHandlerClassOnly(@NotNull Class<?> klass) {
        this.handlerFilter = (pkg, cls) -> pkg.equals(klass.getPackageName()) && cls.equals(klass.getSimpleName());
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
        this.interceptorFilter = (pkg, cls) -> pkg.startsWith(packageName);
    }

    public void setInterceptorClassOnly(@NotNull Class<?> klass) {
        this.interceptorFilter = (pkg, cls) -> pkg.equals(klass.getPackageName()) && cls.equals(klass.getSimpleName());
    }

    public void setInterceptorFilter(@NotNull BiPredicate<String, String> filter) {
        this.interceptorFilter = filter;
    }

    @Override
    public @NotNull QueryParser urlParser() {
        return urlParser;
    }

    public void setUrlParser(@NotNull QueryParser urlParser) {
        this.urlParser = urlParser;
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
    public @NotNull Render defaultRender() {
        return defaultRender;
    }

    public void setDefaultRender(@NotNull Render defaultRender) {
        this.defaultRender = defaultRender;
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
    public boolean getBoolProperty(@NotNull String key) {
        return getBoolProperty(key, false);
    }

    @Override
    public boolean getBoolProperty(@NotNull String key, boolean def) {
        String property = getProperty(key);
        return property != null ? Boolean.parseBoolean(property) : def;
    }

    @Override
    public @NotNull List<Path> getViewPaths(@NotNull String key) {
        String property = getProperty(key);
        return property != null ?
                Arrays.stream(property.split(File.pathSeparator)).map(Path::of).toList() :
                viewPaths();
    }
}
