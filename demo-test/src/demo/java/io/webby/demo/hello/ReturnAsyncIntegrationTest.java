package io.webby.demo.hello;

import io.spbx.webby.testing.BaseHttpIntegrationTest;
import io.spbx.util.testing.TestingBasics;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.spbx.webby.testing.AssertResponse.assertThat;

@Tag("slow")
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
    public void consumer_simple() {
        assertThat(get("/r/async/consumer/simple")).is200().hasContent("OutputStream!");
    }

    @Test
    public void consumer_singles() {
        assertThat(get("/r/async/consumer/singles/0")).is200().hasContent("");
        assertThat(get("/r/async/consumer/singles/1")).is200().hasContent("0");
        assertThat(get("/r/async/consumer/singles/2")).is200().hasContent("01");
        assertThat(get("/r/async/consumer/singles/5")).is200().hasContent("01234");
        assertThat(get("/r/async/consumer/singles/10")).is200().hasContent("0123456789");
    }

    @Test
    public void consumer_buffer_reuse() {
        assertThat(get("/r/async/consumer/buffer/reuse/1/4/1234")).is200().hasContent("1234");
        assertThat(get("/r/async/consumer/buffer/reuse/1/3/1234")).is200().hasContent("1234");
        assertThat(get("/r/async/consumer/buffer/reuse/1/2/1234")).is200().hasContent("1234");
        assertThat(get("/r/async/consumer/buffer/reuse/1/1/1234")).is200().hasContent("1234");

        assertThat(get("/r/async/consumer/buffer/reuse/1/8/1234")).is200().hasContent("1234");
        assertThat(get("/r/async/consumer/buffer/reuse/2/8/1234")).is200().hasContent("12341234");
        assertThat(get("/r/async/consumer/buffer/reuse/3/8/1234")).is200().hasContent("123412341234");

        assertThat(get("/r/async/consumer/buffer/reuse/1/4/1234")).is200().hasContent("1234");
        assertThat(get("/r/async/consumer/buffer/reuse/2/4/1234")).is200().hasContent("12341234");
        assertThat(get("/r/async/consumer/buffer/reuse/3/4/1234")).is200().hasContent("123412341234");

        assertThat(get("/r/async/consumer/buffer/reuse/1/8/123456")).is200().hasContent("123456");
        assertThat(get("/r/async/consumer/buffer/reuse/2/8/123456")).is200().hasContent("123456123456");
        assertThat(get("/r/async/consumer/buffer/reuse/3/8/123456")).is200().hasContent("123456123456123456");
        assertThat(get("/r/async/consumer/buffer/reuse/4/8/123456")).is200().hasContent("123456123456123456123456");
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
