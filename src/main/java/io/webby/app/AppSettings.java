package io.webby.app;

import io.routekit.QueryParser;
import io.routekit.SimpleQueryParser;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public final class AppSettings implements Settings {
    private boolean devMode = true;
    private String webPath = "/web/";
    private Predicate<String> packageTester = null;
    private QueryParser urlParser = SimpleQueryParser.DEFAULT;

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
    public Predicate<String> packageTester() {
        return packageTester;
    }

    public void setPackage(@NotNull String packageName) {
        this.packageTester = pkg -> pkg.startsWith(packageName);
    }

    public void setPackageTester(@NotNull Predicate<String> packageTester) {
        this.packageTester = packageTester;
    }

    @Override
    public QueryParser urlParser() {
        return urlParser;
    }

    public void setUrlParser(@NotNull QueryParser urlParser) {
        this.urlParser = urlParser;
    }
}
