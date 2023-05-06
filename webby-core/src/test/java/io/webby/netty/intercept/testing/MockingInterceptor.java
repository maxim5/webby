package io.webby.netty.intercept.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.Truth;
import io.netty.handler.codec.http.HttpResponse;
import io.webby.netty.errors.ServeException;
import io.webby.netty.intercept.Interceptor;
import io.webby.netty.request.MutableHttpRequestEx;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MockingInterceptor implements Interceptor {
    protected final int id;
    protected final AtomicReference<List<String>> events;

    public MockingInterceptor() {
        this(0, new ArrayList<>());
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

    @Override
    public void enter(@NotNull MutableHttpRequestEx request) throws ServeException {
        registerEvent("%d:enter".formatted(id));
    }

    @Override
    public @NotNull HttpResponse exit(@NotNull MutableHttpRequestEx request, @NotNull HttpResponse response) {
        registerEvent("%d:exit".formatted(id));
        return Interceptor.super.exit(request, response);
    }

    @Override
    public void cleanup() {
        registerEvent("%d:cleanup".formatted(id));
    }

    private void registerEvent(@NotNull String event) {
        events.get().add(event);
    }

    public @NotNull ImmutableList<String> events() {
        return ImmutableList.copyOf(events.get());
    }

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

        public @NotNull IterableSubject assertEvents() {
            return Truth.assertThat(events);
        }
    }
}
