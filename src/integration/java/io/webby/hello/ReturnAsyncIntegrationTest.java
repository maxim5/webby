package io.webby.hello;

import io.webby.netty.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.webby.AssertResponse.assert200;
import static io.webby.AssertResponse.assert500;

public class ReturnAsyncIntegrationTest extends BaseIntegrationTest {
    private final ReturnAsync instance = testStartup(ReturnAsync.class).getInstance(ReturnAsync.class);

    @Test
    public void futures() {
        assert200(get("/r/async/futures/simple"), "Success!");
        assert200(get("/r/async/futures/completable"), "Complete");
        assert200(get("/r/async/futures/task"), "Task");
        assert200(get("/r/async/futures/immediate"), "Now");
        assert200(get("/r/async/futures/listenable"), "Listen");
        assert200(get("/r/async/futures/netty"), "Netty");
        assert200(get("/r/async/futures/promise"), "Promise");
    }

    @Test
    public void futures_timeout() {
        assert200(get("/r/async/futures/timeout/200"), "Thanks for waiting");
    }

    @Test
    public void futures_error() {
        assert500(get("/r/async/futures/error"));
    }

    @Test
    public void futures_error_timeout() {
        assert500(get("/r/async/futures/timeout/200/error"));
    }

    @Test
    public void consumer() {
        assert200(get("/r/async/consumer/simple"), "OutputStream!");
    }

    @Test
    public void consumer_timeout() {
        assert200(get("/r/async/consumer/timeout/200"), "DelayedOutput.");
    }

    @Override
    protected void flushChannel() {
        do {
            Thread.yield();
        } while (!instance.executor.getQueue().isEmpty());
        // Thread.sleep(50);
        super.flushChannel();
    }
}
