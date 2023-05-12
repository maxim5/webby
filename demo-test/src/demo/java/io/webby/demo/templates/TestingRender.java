package io.webby.demo.templates;

import com.google.common.truth.CustomSubjectBuilder;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.app.AppSettings;
import io.webby.netty.HttpConst;
import io.webby.perf.stats.Stat;
import io.webby.testing.AssertResponse;
import io.webby.testing.Testing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                settings.setProfileMode(this == Config.BytesStatsEnabled);
                settings.setStreamingEnabled(this == Config.StreamingEnabled);
            };
        }
    }

    @CheckReturnValue
    public static @NotNull RenderedHttpResponseSubject assertThat(@Nullable HttpResponse response) {
        return Truth.assertAbout(RenderedHttpResponseSubjectBuilder::new).that(response);
    }

    @CanIgnoreReturnValue
    public static class RenderedHttpResponseSubject extends AssertResponse.HttpResponseSubject {
        public RenderedHttpResponseSubject(@NotNull FailureMetadata metadata, @Nullable HttpResponse response) {
            super(metadata, response);
        }

        public @NotNull RenderedHttpResponseSubject hasSimpleStatsHeaderForCurrentConfig() {
            return assertSimpleStatsHeader(currentConfig());
        }

        public @NotNull RenderedHttpResponseSubject assertSimpleStatsHeader(@NotNull Config config) {
            if (config == Config.BytesStatsEnabled) {
                assertStatsHeader(response(), Stat.DB_SET);
            } else {
                assertNoStatsHeaders(response());
            }
            return this;
        }

        public @NotNull RenderedHttpResponseSubject hasRenderedStatsHeaderForCurrentConfig() {
            return hasRenderedStatsHeader(currentConfig());
        }

        public @NotNull RenderedHttpResponseSubject hasRenderedStatsHeader(@NotNull Config config) {
            if (config == Config.BytesStatsEnabled) {
                assertStatsHeader(response(), Stat.DB_SET, Stat.RENDER);
            } else {
                assertNoStatsHeaders(response());
            }
            return this;
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

    private static class RenderedHttpResponseSubjectBuilder extends CustomSubjectBuilder {
        protected RenderedHttpResponseSubjectBuilder(FailureMetadata metadata) {
            super(metadata);
        }

        public @NotNull RenderedHttpResponseSubject that(@Nullable HttpResponse response) {
            return new RenderedHttpResponseSubject(metadata(), response);
        }
    }
}
