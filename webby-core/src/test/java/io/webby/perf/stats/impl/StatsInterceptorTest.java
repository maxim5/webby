package io.webby.perf.stats.impl;

import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.webby.netty.HttpConst;
import io.webby.netty.intercept.attr.Attributes;
import io.webby.netty.request.DefaultHttpRequestEx;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.Testing;
import io.webby.testing.ext.FluentLoggingCapture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.google.common.truth.Truth.assertThat;

public class StatsInterceptorTest {
    @RegisterExtension static final FluentLoggingCapture LOGGING = new FluentLoggingCapture(StatsInterceptor.class);

    private final StatsInterceptor interceptor = Testing.testStartup().getInstance(StatsInterceptor.class);

    @Test
    public void lifecycle_enter_exit() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").ex();

        assertThat(LocalStatsHolder.localStatsRef.get()).isNull();
        assertThat(request.attr(Attributes.Stats)).isNull();

        interceptor.enter(request);
        assertThat(LocalStatsHolder.localStatsRef.get()).isNotNull();
        assertThat(request.attr(Attributes.Stats)).isSameInstanceAs(LocalStatsHolder.localStatsRef.get());

        HttpResponse response = interceptor.exit(request, new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        assertThat(LocalStatsHolder.localStatsRef.get()).isNull();
        assertThat(response.headers().get(HttpConst.SERVER_TIMING)).isNotNull();
    }

    @Test
    public void lifecycle_enter_cleanup() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").ex();

        assertThat(LocalStatsHolder.localStatsRef.get()).isNull();
        assertThat(request.attr(Attributes.Stats)).isNull();

        interceptor.enter(request);
        assertThat(LocalStatsHolder.localStatsRef.get()).isNotNull();
        assertThat(request.attr(Attributes.Stats)).isSameInstanceAs(LocalStatsHolder.localStatsRef.get());

        interceptor.cleanup();
        assertThat(LocalStatsHolder.localStatsRef.get()).isNull();
    }

    @Test
    public void lifecycle_enter_thread_local_not_empty() {
        DefaultHttpRequestEx request = HttpRequestBuilder.get("/foo").ex();
        LocalStatsHolder.localStatsRef.set(new StatsCollector(555));

        interceptor.enter(request);
        assertThat(request.attr(Attributes.Stats)).isSameInstanceAs(LocalStatsHolder.localStatsRef.get());
        assertThat(LOGGING.logRecordsContaining("This thread contains a dirty local-stats")).isNotEmpty();
    }
}
