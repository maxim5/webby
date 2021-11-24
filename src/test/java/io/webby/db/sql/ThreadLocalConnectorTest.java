package io.webby.db.sql;

import com.google.common.flogger.FluentLogger;
import io.webby.db.sql.testing.FakeConnectionPool;
import io.webby.db.sql.testing.SimpleConnection;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadLocalConnectorTest {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private final FakeConnectionPool<SimpleConnection> pool = new FakeConnectionPool<>(SimpleConnection::new);
    private final ThreadLocalConnector connector = new ThreadLocalConnector(pool, -1);

    @BeforeEach
    void setUp() {
        ThreadLocalConnector.cleanupIfNecessary();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10, 20})
    public void single_thread_only_calls(int requests) {
        new Worker(requests, 25, connector, () -> {}, () -> {}).run();
        assertSingleThreadAccessAndClosed(pool.connections(), requests == 0 ? 0 : 1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10, 20})
    public void single_thread_refresh_before(int requests) {
        new Worker(requests, 25, connector, connector::refreshIfNecessary, () -> {}).run();
        assertSingleThreadAccessAndClosed(pool.connections(), requests);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10, 20})
    public void single_thread_cleanup_after(int requests) {
        new Worker(requests, 25, connector, () -> {}, ThreadLocalConnector::cleanupIfNecessary).run();
        assertSingleThreadAccessAndClosed(pool.connections(), requests);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10, 20})
    public void single_thread_refresh_and_cleanup(int requests) {
        new Worker(requests, 25, connector, connector::refreshIfNecessary, ThreadLocalConnector::cleanupIfNecessary).run();
        assertSingleThreadAccessAndClosed(pool.connections(), requests);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 50, 100, 1000, 10000})
    public void multi_threads_refresh_before(int requests) throws Exception {
        executeWorkers(requests, 5, () -> new Worker(1, 25, connector, connector::refreshIfNecessary, () -> {}));
        assertSingleThreadAccessAndClosed(pool.connections(), requests);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 50, 100, 1000, 10000})
    public void multi_threads_cleanup_after(int requests) throws Exception {
        executeWorkers(requests, 5, () -> new Worker(1, 25, connector, () -> {}, ThreadLocalConnector::cleanupIfNecessary));
        assertSingleThreadAccessAndClosed(pool.connections(), requests);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 50, 100, 1000, 10000})
    public void multi_threads_refresh_and_cleanup(int requests) throws Exception {
        executeWorkers(requests, 5, () -> new Worker(1, 25, connector, connector::refreshIfNecessary, ThreadLocalConnector::cleanupIfNecessary));
        assertSingleThreadAccessAndClosed(pool.connections(), requests);
    }

    private record Worker(int requests, int callsPerRequest,
                          @NotNull ThreadLocalConnector connector,
                          Runnable beforeCalls, Runnable afterCalls) implements Runnable {
        @Override
        public void run() {
            log.at(Level.FINE).log("Starting %d", Thread.currentThread().getId());
            SimpleConnection last = null;

            for (int req = 0; req < requests; req++) {
                beforeCalls.run();
                for (int call = 0; call < callsPerRequest; call++) {
                    last = (SimpleConnection) connector.connection();
                    assertFalse(last.isClosed());
                }
                afterCalls.run();
            }

            if (last != null && !last.isClosed()) {
                last.close();
            }
            log.at(Level.FINE).log("Complete %d", Thread.currentThread().getId());
        }
    }

    private static void executeWorkers(int requests, int threads, @NotNull Supplier<Runnable> supplier) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < requests; i++) {
            executor.execute(supplier.get());
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(100, TimeUnit.MILLISECONDS));
    }

    private static void assertSingleThreadAccessAndClosed(@NotNull List<SimpleConnection> connections, int expected) {
        assertEquals(expected, connections.size());
        assertTrue(connections.stream().allMatch(SimpleConnection::isSingleThreadAccess),
                   () -> "Not all connections are accessed by only one thread. Connections: %s".formatted(connections));
        assertTrue(connections.stream().allMatch(SimpleConnection::isClosed),
                   () -> "Not all connections are closed. Connections: %s".formatted(connections));
    }
}
