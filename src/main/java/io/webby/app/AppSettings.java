package io.webby.app;

import io.routekit.SimpleQueryParser;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public final class AppSettings {
    private boolean devMode = true;
    private String webPath = "/web/";
    private Predicate<String> packageTester = null;
    private SimpleQueryParser urlParser = SimpleQueryParser.DEFAULT;

    public boolean isDevMode() {
        return devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public String webPath() {
        return webPath;
    }

    public void setWebPath(@NotNull String webPath) {
        this.webPath = webPath;
    }

    public Predicate<String> packageTester() {
        return packageTester;
    }

    public void setPackage(@NotNull String packageName) {
        this.packageTester = pkg -> pkg.startsWith(packageName);
    }

    public void setPackageTester(@NotNull Predicate<String> packageTester) {
        this.packageTester = packageTester;
    }

    public SimpleQueryParser urlParser() {
        return urlParser;
    }

    public void setUrlParser(@NotNull SimpleQueryParser urlParser) {
        this.urlParser = urlParser;
    }
}
