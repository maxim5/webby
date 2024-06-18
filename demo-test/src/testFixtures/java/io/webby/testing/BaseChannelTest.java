package io.webby.testing;

import com.google.inject.Injector;
import io.netty.channel.embedded.EmbeddedChannel;
import io.spbx.util.testing.TestingBytes;
import io.webby.app.AppSettings;
import io.webby.demo.DevPaths;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BaseChannelTest {
    private static final String EXAMPLES_WEB_PATH = DevPaths.DEMO_WEB;
    private static final String EXAMPLES_VIEW_PATH = DevPaths.DEMO_WEB;

    protected Injector injector;
    protected EmbeddedChannel channel;

    protected abstract class GuiceSetup {
        public GuiceSetup(@NotNull Injector injector) {
            BaseChannelTest.this.injector = injector;
        }
    }

    protected class SingleTargetSetup<T> extends GuiceSetup {
        protected final Class<T> klass;

        public SingleTargetSetup(@NotNull Injector injector, @NotNull Class<T> klass) {
            super(injector);
            this.klass = klass;
        }
    }

    protected static final Consumer<AppSettings> DEFAULT_SETTINGS = settings -> {
        settings.setWebPath(EXAMPLES_WEB_PATH);
        settings.setViewPath(EXAMPLES_VIEW_PATH);
    };

    protected void assertChannelInitialized() {
        assertNotNull(channel, "Channel is not initialized. Add @BeforeEach setup method calling testStartup()");
    }

    protected static <T> @NotNull Queue<T> readAllOutbound(@NotNull EmbeddedChannel channel) {
        Queue<T> result = new ArrayDeque<>();
        T object;
        while ((object = channel.readOutbound()) != null) {
            result.add(TestingBytes.replaceWithReadable(object));
        }
        return result;
    }

    protected void flushChannel() {
        if (channel.isOpen()) {
            channel.flushOutbound();
        }
    }
}
