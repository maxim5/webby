package io.webby.testing;

import com.google.inject.Injector;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.embedded.EmbeddedChannel;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayDeque;
import java.util.Queue;

import static io.webby.util.EasyCast.castAny;

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
            if (Testing.READABLE && object instanceof ByteBufHolder holder) {
                object = castAny(holder.replace(FakeRequests.readable(holder.content())));
            }
            result.add(object);
        }
        return result;
    }

    protected void flushChannel() {
        channel.flushOutbound();
    }
}
