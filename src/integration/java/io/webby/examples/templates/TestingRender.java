package io.webby.examples.templates;

import io.netty.handler.codec.http.HttpResponse;
import io.webby.app.AppSettings;
import io.webby.perf.stats.Stat;
import io.webby.testing.Testing;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static io.webby.testing.AssertStats.assertNoStatsHeaders;
import static io.webby.testing.AssertStats.assertStatsHeader;

public class TestingRender {
    enum Config {
        BytesStatsEnabled,
        BytesStatsDisabled,
        StreamingEnabled;

        public @NotNull Consumer<AppSettings> asSettingsUpdater() {
            return settings -> {
                settings.setProfileMode(this != Config.BytesStatsDisabled);
                settings.setStreamingEnabled(this == Config.StreamingEnabled);
            };
        }
    }

    public static @NotNull Config currentConfig() {
        return Testing.Internals.getInstance(Config.class);
    }

    public static void assertSimpleStatsHeaderForCurrentConfig(@NotNull HttpResponse response) {
        assertSimpleStatsHeader(response, currentConfig());
    }

    public static void assertSimpleStatsHeader(@NotNull HttpResponse response, @NotNull Config config) {
        if (config == Config.BytesStatsDisabled) {
            assertNoStatsHeaders(response);
        } else {
            assertStatsHeader(response, Stat.DB_SET);
        }
    }

    public static void assertRenderedStatsHeaderForCurrentConfig(@NotNull HttpResponse response) {
        assertRenderedStatsHeader(response, currentConfig());
    }

    public static void assertRenderedStatsHeader(@NotNull HttpResponse response, @NotNull Config config) {
        if (config == Config.BytesStatsDisabled) {
            assertNoStatsHeaders(response);
        } else {
            assertStatsHeader(response, Stat.DB_SET, Stat.RENDER);
        }
    }
}
