package io.webby.app;

import io.routekit.QueryParser;
import io.routekit.SimpleQueryParser;
import io.webby.url.annotate.Marshal;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public final class AppSettings implements Settings {
    private boolean devMode = true;
    private String webPath = "/web/";
    private BiPredicate<String, String> filter = null;
    private QueryParser urlParser = SimpleQueryParser.DEFAULT;
    private Marshal defaultRequestContentMarshal = Marshal.JSON;
    private Marshal defaultResponseContentMarshal = Marshal.AS_STRING;

    @Override
    public boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    @Override
    public String webPath() {
        return webPath;
    }

    public void setWebPath(@NotNull String webPath) {
        this.webPath = webPath;
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
}
