package io.webby.testing;

import com.google.errorprone.annotations.CheckReturnValue;
import io.webby.util.func.ThrowConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A {@link Consumer} and {@link ThrowConsumer} implementation that allows to set up expectations
 * (how many times calls, what args, etc.) as well as track that all consumers are called up until the end.
 *
 * @param <T> type of items accepted by the consumer
 * @param <E> exception to throw if a mock for {@link ThrowConsumer} is necessary.
 */
public class MockConsumer<T, E extends Throwable> implements Consumer<T>, ThrowConsumer<T, E>, AutoCloseable {
    private final List<T> items = new ArrayList<>();
    private List<T> expected;

    public MockConsumer() {
        if (!Tracker.trackers.isEmpty()) {
            Tracker.trackers.peek().add(this);
        }
    }

    @CheckReturnValue
    public static <T> MockConsumer<T, RuntimeException> mock() {
        return new MockConsumer<>();
    }

    @SafeVarargs
    @CheckReturnValue
    public static <T> MockConsumer<T, RuntimeException> expecting(@Nullable T @NotNull ... expected) {
        return new MockConsumer<T, RuntimeException>().expect(expected);
    }

    /**
     * To be used for {@code ThrowConsumer} mocks.
     */
    public static class Throw {
        @CheckReturnValue
        public static <T, E extends Throwable> MockConsumer<T, E> mock() {
            return new MockConsumer<>();
        }
        @SafeVarargs
        @CheckReturnValue
        public static <T, E extends Throwable> MockConsumer<T, E> expecting(@Nullable T @NotNull ... expected) {
            return new MockConsumer<T, E>().expect(expected);
        }
    }

    @SafeVarargs
    public final @NotNull MockConsumer<T, E> expect(@Nullable T @NotNull ... expected) {
        this.expected = Arrays.asList(expected);
        return this;
    }

    @Override
    public void accept(T item) {
        if (expected != null) {
            assertTrue(expected.size() > items.size(), "Unexpected item added: " + item);
            assertEquals(expected.get(items.size()), item);
        }
        items.add(item);
    }

    public @NotNull List<T> items() {
        return items;
    }

    public void assertAllDone() {
        if (expected != null) {
            assertThat(items).containsExactlyElementsIn(expected);
        }
    }

    @Override
    public void close() {
        assertAllDone();
    }

    public static @NotNull MockConsumer.Tracker trackAllConsumersDone() {
        return new Tracker();
    }

    public static class Tracker implements AutoCloseable {
        private static final Stack<Tracker> trackers = new Stack<>();
        private final List<MockConsumer<?, ?>> mocks = new ArrayList<>();

        public Tracker() {
            trackers.push(this);
        }

        @Override
        public void close() {
            mocks.forEach(MockConsumer::close);
            trackers.pop();
        }

        public void add(@NotNull MockConsumer<?, ?> mockConsumer) {
            mocks.add(mockConsumer);
        }
    }
}
