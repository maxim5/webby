package io.webby.demo.hello;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.HttpConst;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.ext.HppcIterationSeedExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static io.webby.testing.AssertResponse.assertThat;

public class RequestAttrIntegrationTest extends BaseHttpIntegrationTest {
    @RegisterExtension static final HppcIterationSeedExtension ITERATION_SEED = new HppcIterationSeedExtension();

    protected final RequestAttr handler = testSetup(RequestAttr.class).initHandler();

    @Test
    public void attributes() {
        FullHttpRequest request = HttpRequestBuilder.get("/attr/get").withHeader(HttpConst.USER_AGENT, "foobar").full();
        HttpResponse response = call(request);
        assertThat(response).is200().hasContent("stats:StatsCollector, session:foobar, user:null");
    }

    @Test
    public void stats() {
        HttpResponse response = get("/attr/stats");
        assertThat(response).is200().hasStatsHeaderWhichMatches("main;desc=\"\\{db_set:1,db_get:777,time:\\d+\\}\"");
    }
}
