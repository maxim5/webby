package io.webby.netty;

import com.google.inject.Injector;
import io.netty.channel.embedded.EmbeddedChannel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayDeque;
import java.util.Queue;

public class BaseChannelTest {
    protected Injector injector;
    protected EmbeddedChannel channel;

    protected void assertChannelInitialized() {
        Assertions.assertNotNull(channel,
            "Channel is not initialized. Add @BeforeEach setup method calling testStartup()");
    }

    @NotNull
    protected static <T> Queue<T> readAllOutbound(@NotNull EmbeddedChannel channel) {
        Queue<T> result = new ArrayDeque<>();
        T object;
        while ((object = channel.readOutbound()) != null) {
            result.add(object);
        }
        return result;
    }

    protected void flushChannel() {
        channel.flushOutbound();
    }
}
