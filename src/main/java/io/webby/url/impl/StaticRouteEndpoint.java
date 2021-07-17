package io.webby.url.impl;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.netty.StaticServing;
import io.webby.url.caller.Caller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class StaticRouteEndpoint implements RouteEndpoint {
    private final EndpointCaller caller;
    private final StaticServing serving;

    public StaticRouteEndpoint(@NotNull String path, @NotNull StaticServing serving) {
        this.serving = serving;
        caller = new EndpointCaller(new StaticCaller(path), EndpointOptions.DEFAULT);
    }

    @Override
    @Nullable
    public EndpointCaller getAcceptedCallerOrNull(@NotNull HttpRequest request) {
        return serving.accept(request.method()) ? caller : null;
    }

    @Override
    @NotNull
    public String describe() {
        return caller.caller().method().toString();
    }

    private class StaticCaller implements Caller {
        private final String path;

        private StaticCaller(@NotNull String path) {
            this.path = path;
        }

        @Override
        public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharBuffer> variables) throws Exception {
            return serving.serve(path, request);
        }

        @Override
        @NotNull
        public Object method() {
            return path;
        }
    }
}
