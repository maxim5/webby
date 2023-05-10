package io.webby.demo.hello;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.HttpConst;
import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.HttpRequestBuilder;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.assert200;
import static io.webby.testing.AssertStats.assertStatsHeaderMatches;

public class RequestAttrIntegrationTest extends BaseHttpIntegrationTest {
    protected final RequestAttr handler = testSetup(RequestAttr.class).initHandler();

    @Test
    public void attributes() {
        FullHttpRequest request = HttpRequestBuilder.get("/attr/get").withHeader(HttpConst.USER_AGENT, "foobar").full();
        assert200(call(request), "stats:StatsCollector, session:foobar, user:null");
    }

    @Test
    public void stats() {
        HttpResponse response = get("/attr/stats");
        assert200(response);
        assertStatsHeaderMatches(response, "main;desc=\"\\{db_set:1,db_get:777,time:\\d+\\}\"");
    }
}
