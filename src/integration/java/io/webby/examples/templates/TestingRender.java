package io.webby.examples.templates;

import io.webby.app.AppSettings;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class TestingRender {
    enum Config {
        BytesStatsEnabled,
        BytesStatsDisabled,
        StreamingEnabled;

        public @NotNull Consumer<AppSettings> asSettingsUpdater() {
            return settings -> {
                settings.setDevMode(this != Config.BytesStatsDisabled);
                settings.setStreamingEnabled(this == Config.StreamingEnabled);
            };
        }
    }
}
