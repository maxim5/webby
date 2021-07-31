package io.webby.url.impl;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.routekit.util.CharBuffer;
import io.webby.netty.response.StaticServing;
import io.webby.url.caller.Caller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public final class StaticRouteEndpoint implements RouteEndpoint {
    private static final EndpointContext EMPTY_CONTEXT = new EndpointContext(Collections.emptyMap(), true, false);

    private final Endpoint endpoint;
    private final StaticServing serving;

    public StaticRouteEndpoint(@NotNull String path, @NotNull StaticServing serving) {
        this.serving = serving;
        endpoint = new Endpoint(new StaticCaller(path), EMPTY_CONTEXT, EndpointOptions.DEFAULT);
    }

    @Override
    @Nullable
    public Endpoint getAcceptedEndpointOrNull(@NotNull HttpRequest request) {
        return serving.accept(request.method()) ? endpoint : null;
    }

    @Override
    @NotNull
    public String describe() {
        return endpoint.caller().method().toString();
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
