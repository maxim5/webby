package io.spbx.webby.demo.templates;

import com.google.common.truth.CustomSubjectBuilder;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.spbx.webby.app.AppSettings;
import io.spbx.webby.app.Settings.Toggle;
import io.spbx.webby.netty.HttpConst;
import io.spbx.webby.perf.stats.Stat;
import io.spbx.webby.testing.AssertResponse.HttpResponseSubject;
import io.spbx.webby.testing.Testing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TestingRender {
    public enum Config {
        BytesStatsEnabled,
        BytesStatsDisabled,
        StreamingEnabled;

        public @NotNull Consumer<AppSettings> asSettingsUpdater() {
            return settings -> {
                settings.setProfileMode(Toggle.from(this == Config.BytesStatsEnabled));
                settings.setStreamingEnabled(this == Config.StreamingEnabled);
            };
        }
    }

    @CheckReturnValue
    public static @NotNull RenderedHttpResponseSubject assertThat(@Nullable HttpResponse response) {
        return Truth.assertAbout(metadata -> new CustomSubjectBuilder(metadata) {
            public @NotNull RenderedHttpResponseSubject that(@Nullable HttpResponse response) {
                return new RenderedHttpResponseSubject(metadata(), response);
            }
        }).that(response);
    }

    @CanIgnoreReturnValue
    public static class RenderedHttpResponseSubject extends HttpResponseSubject<RenderedHttpResponseSubject> {
        public RenderedHttpResponseSubject(@NotNull FailureMetadata metadata, @Nullable HttpResponse response) {
            super(metadata, response);
        }

        public @NotNull RenderedHttpResponseSubject hasSimpleStatsHeaderForCurrentConfig() {
            return assertSimpleStatsHeader(currentConfig());
        }

        public @NotNull RenderedHttpResponseSubject assertSimpleStatsHeader(@NotNull Config config) {
            if (config == Config.BytesStatsEnabled) {
                return hasStatsHeader(Stat.DB_SET);
            } else {
                return hasNoStatsHeaders();
            }
        }

        public @NotNull RenderedHttpResponseSubject hasRenderedStatsHeaderForCurrentConfig() {
            return hasRenderedStatsHeader(currentConfig());
        }

        public @NotNull RenderedHttpResponseSubject hasRenderedStatsHeader(@NotNull Config config) {
            if (config == Config.BytesStatsEnabled) {
                return hasStatsHeader(Stat.DB_SET, Stat.RENDER);
            } else {
                return hasNoStatsHeaders();
            }
        }

        private static @NotNull Config currentConfig() {
            return Testing.Internals.getInstance(Config.class);
        }

        public @NotNull RenderedHttpResponseSubject matchesHeadersForCurrentConfig(@NotNull HttpResponse manual) {
            HttpHeaders renderedHeaders = headersWithoutVolatile(response().headers());
            HttpHeaders manualHeaders = headersWithoutVolatile(manual.headers());
            if (currentConfig() == Config.StreamingEnabled) {
                manualHeaders.remove(HttpConst.CONTENT_LENGTH);
            }
            Truth.assertThat(renderedHeaders).isEqualTo(manualHeaders);
            return this;
        }

        private static @NotNull HttpHeaders headersWithoutVolatile(@NotNull HttpHeaders headers) {
            return headers.copy()
                .remove(HttpConst.SET_COOKIE)
                .remove(HttpConst.SERVER_TIMING);
        }
    }
}
