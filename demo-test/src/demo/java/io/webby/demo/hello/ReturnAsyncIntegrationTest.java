package io.webby.demo.hello;

import io.webby.testing.BaseHttpIntegrationTest;
import io.webby.testing.TestingBasics;
import org.junit.jupiter.api.Test;

import static io.webby.testing.AssertResponse.assertThat;

public class ReturnAsyncIntegrationTest extends BaseHttpIntegrationTest {
    protected final ReturnAsync handler = testSetup(ReturnAsync.class).initHandler();

    @Test
    public void futures() {
        assertThat(get("/r/async/futures/simple")).is200().hasContent("Success!");
        assertThat(get("/r/async/futures/completable")).is200().hasContent("Complete");
        assertThat(get("/r/async/futures/task")).is200().hasContent("Task");
        assertThat(get("/r/async/futures/immediate")).is200().hasContent("Now");
        assertThat(get("/r/async/futures/listenable")).is200().hasContent("Listen");
        assertThat(get("/r/async/futures/netty")).is200().hasContent("Netty");
        assertThat(get("/r/async/futures/promise")).is200().hasContent("Promise");
    }

    @Test
    public void futures_timeout() {
        assertThat(get("/r/async/futures/timeout/200")).is200().hasContent("Thanks for waiting");
    }

    @Test
    public void futures_error() {
        assertThat(get("/r/async/futures/error")).is500();
    }

    @Test
    public void futures_error_timeout() {
        assertThat(get("/r/async/futures/timeout/200/error")).is500();
    }

    @Test
    public void consumer() {
        assertThat(get("/r/async/consumer/simple")).is200().hasContent("OutputStream!");
    }

    @Test
    public void consumer_timeout() {
        assertThat(get("/r/async/consumer/timeout/200")).is200().hasContent("DelayedOutput.");
    }

    @Override
    protected void flushChannel() {
        TestingBasics.waitFor(20);
        super.flushChannel();
    }
}
