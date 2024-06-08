package io.webby.testing.netty.intercept;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CheckReturnValue;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.errors.ServeException;
import io.webby.netty.intercept.Interceptor;
import io.webby.netty.request.MutableHttpRequestEx;
import io.webby.testing.ForcedFailure;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MockingInterceptor implements Interceptor {
    protected final int id;
    protected final AtomicReference<List<String>> events;

    protected final ForcedFailure enterFailure = new ForcedFailure();
    protected final ForcedFailure exitFailure = new ForcedFailure();
    protected final ForcedFailure cleanupFailure = new ForcedFailure();

    protected MockingInterceptor() {
        this(0);
    }

    public MockingInterceptor(int id) {
        this(id, new ArrayList<>());
    }

    public MockingInterceptor(int id, @NotNull ArrayList<String> events) {
        this.id = id;
        this.events = new AtomicReference<>(events);
    }

    public static @NotNull MockingInterceptor shared(int id, @NotNull ArrayList<String> events) {
        return new MockingInterceptor(id, events);
    }

    public @NotNull MockingInterceptor forceEnterFail(@NotNull Throwable throwable) {
        enterFailure.force(throwable);
        return this;
    }

    public @NotNull MockingInterceptor forceExitFail(@NotNull Throwable throwable) {
        exitFailure.force(throwable);
        return this;
    }

    public @NotNull MockingInterceptor forceCleanupFail(@NotNull Throwable throwable) {
        cleanupFailure.force(throwable);
        return this;
    }

    @Override
    public void enter(@NotNull MutableHttpRequestEx request) throws ServeException {
        registerEvent("%d:enter%s".formatted(id, enterFailure.isSet() ? ":FAIL" : ""));
        enterFailure.throwIfSet();
    }

    @Override
    public @NotNull HttpResponse exit(@NotNull MutableHttpRequestEx request, @NotNull HttpResponse response) {
        registerEvent("%d:exit%s".formatted(id, exitFailure.isSet() ? ":FAIL" : ""));
        exitFailure.throwIfSet();
        return response;
    }

    @Override
    public void cleanup() {
        registerEvent("%d:cleanup%s".formatted(id, cleanupFailure.isSet() ? ":FAIL" : ""));
        cleanupFailure.throwIfSet();
    }

    private void registerEvent(@NotNull String event) {
        events.get().add(event);
    }

    public @NotNull ImmutableList<String> events() {
        return ImmutableList.copyOf(events.get());
    }

    @CheckReturnValue
    public @NotNull IterableSubject assertEvents() {
        return Truth.assertThat(events.get());
    }

    public static class Factory {
        protected final ArrayList<String> events = new ArrayList<>();

        public @NotNull MockingInterceptor spawn(int id) {
            return MockingInterceptor.shared(id, events);
        }

        public <I extends MockingInterceptor> @NotNull I share(@NotNull I interceptor) {
            interceptor.events.set(events);
            return interceptor;
        }

        public @NotNull ImmutableList<String> events() {
            return ImmutableList.copyOf(events);
        }

        @CheckReturnValue
        public @NotNull IterableSubject assertEvents() {
            return Truth.assertThat(events);
        }
    }
}
