package io.webby.url.impl;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.routekit.util.CharArray;
import io.webby.netty.response.StaticServing;
import io.webby.url.caller.Caller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public final class StaticRouteDynamicEndpoint implements RouteEndpoint {
    private static final EndpointContext EMPTY_CONTEXT = new EndpointContext(Collections.emptyMap(), true, false);

    private final Endpoint endpoint;
    private final StaticServing serving;

    public StaticRouteDynamicEndpoint(@NotNull String prefix, @NotNull StaticServing serving) {
        this.serving = serving;
        endpoint = new Endpoint(new DynamicCaller(prefix), EMPTY_CONTEXT, EndpointOptions.DEFAULT);
    }

    @Override
    public @Nullable Endpoint getAcceptedEndpointOrNull(@NotNull HttpRequest request) {
        return serving.accept(request.method()) ? endpoint : null;
    }

    @Override
    public @NotNull String describe() {
        return endpoint.caller().method().toString();
    }

    private class DynamicCaller implements Caller {
        private final String prefix;

        private DynamicCaller(@NotNull String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) throws Exception {
            CharArray path = variables.get("path");
            return serving.serve(path.toString(), request);
        }

        @Override
        public @NotNull Object method() {
            return "DynamicCaller:" + prefix;
        }
    }
}
