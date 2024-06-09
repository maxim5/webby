package io.webby.demo.hello;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.HttpRequestBuilder;
import io.webby.testing.ext.HppcReflectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static io.webby.testing.AssertResponse.assertThat;

public class RequestAttrIntegrationTest extends BaseHttpIntegrationTest {
    @RegisterExtension private static final HppcReflectionExtension HPPC_ORDER_FIX = new HppcReflectionExtension();

    protected final RequestAttr handler = testSetup(RequestAttr.class).initHandler();

    @Test
    public void attributes() {
        FullHttpRequest request = HttpRequestBuilder.get("/attr/get").withUserAgent("foobar").full();
        HttpResponse response = call(request);
        assertThat(response).is200().hasContent("stats:StatsCollector, session:foobar, user:null");
    }

    @Test
    public void stats() {
        HttpResponse response = get("/attr/stats");
        assertThat(response).is200().hasStatsHeaderWhichMatches("main;desc=\"\\{db_set:1,db_get:777,time:\\d+\\}\"");
    }
}
