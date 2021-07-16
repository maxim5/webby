package io.webby.app;

public final class AppSettings {
    private boolean devMode = true;
    private String webPath = "/web/";

    public boolean devMode() {
        return devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public String webPath() {
        return webPath;
    }

    public void setWebPath(String webPath) {
        this.webPath = webPath;
    }
}
