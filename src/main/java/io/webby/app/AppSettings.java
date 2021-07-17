package io.webby.app;

import io.routekit.SimpleQueryParser;
import org.jetbrains.annotations.NotNull;

public final class AppSettings {
    private boolean devMode = true;
    private String webPath = "/web/";
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

    public SimpleQueryParser urlParser() {
        return urlParser;
    }

    public void setUrlParser(@NotNull SimpleQueryParser urlParser) {
        this.urlParser = urlParser;
    }
}
