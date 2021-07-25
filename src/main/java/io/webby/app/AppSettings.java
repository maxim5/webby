package io.webby.app;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.routekit.QueryParser;
import io.routekit.SimpleQueryParser;
import io.webby.url.annotate.Marshal;
import io.webby.url.annotate.Render;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private Path webPath = Path.of("/web/");
    private List<Path> viewPaths = List.of(Path.of("/web/"));
    private Charset charset = Charset.defaultCharset();
    private BiPredicate<String, String> filter = null;
    private QueryParser urlParser = SimpleQueryParser.DEFAULT;
    private Marshal defaultRequestContentMarshal = Marshal.JSON;
    private Marshal defaultResponseContentMarshal = Marshal.AS_STRING;
    private Render defaultRender = Render.JTE;

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
        hotReloadDefault = false;
    }

    @Override
    public Path webPath() {
        return webPath;
    }

    public void setWebPath(@NotNull Path webPath) {
        this.webPath = webPath;
    }

    public void setWebPath(@NotNull String webPath) {
        this.webPath = Path.of(webPath);
    }

    @Override
    public List<Path> viewPaths() {
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
    public Charset charset() {
        return charset;
    }

    public void setCharset(@NotNull Charset charset) {
        this.charset = charset;
    }

    @Override
    public BiPredicate<String, String> filter() {
        return filter;
    }

    public void setPackageOnly(@NotNull String packageName) {
        this.filter = (pkg, cls) -> pkg.startsWith(packageName);
    }

    public void setClassOnly(@NotNull Class<?> klass) {
        this.filter = (pkg, cls) -> pkg.equals(klass.getPackageName()) && cls.equals(klass.getSimpleName());
    }

    public void setFilter(@NotNull BiPredicate<String, String> filter) {
        this.filter = filter;
    }

    @Override
    public QueryParser urlParser() {
        return urlParser;
    }

    public void setUrlParser(@NotNull QueryParser urlParser) {
        this.urlParser = urlParser;
    }

    @Override
    public Marshal defaultRequestContentMarshal() {
        return defaultRequestContentMarshal;
    }

    public void setDefaultRequestContentMarshal(@NotNull Marshal marshal) {
        this.defaultRequestContentMarshal = marshal;
    }

    @Override
    public Marshal defaultResponseContentMarshal() {
        return defaultResponseContentMarshal;
    }

    public void setDefaultResponseContentMarshal(@NotNull Marshal marshal) {
        this.defaultResponseContentMarshal = marshal;
    }

    @Override
    public Render defaultRender() {
        return defaultRender;
    }

    public void setDefaultRender(@NotNull Render defaultRender) {
        this.defaultRender = defaultRender;
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
}
