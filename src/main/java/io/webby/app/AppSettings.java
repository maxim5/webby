package io.webby.app;

public final class AppSettings {
    private boolean devMode;

    public boolean devMode() {
        return devMode;
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }
}
