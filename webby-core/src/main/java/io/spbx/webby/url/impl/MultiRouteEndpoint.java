package io.spbx.webby.url.impl;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.spbx.webby.url.UrlConfigError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

record MultiRouteEndpoint(@NotNull Map<HttpMethod, SingleRouteEndpoint> map,
                          @Nullable SingleRouteEndpoint def) implements RouteEndpoint {
    @Override
    public @Nullable Endpoint getAcceptedEndpointOrNull(@NotNull HttpRequest request) {
        SingleRouteEndpoint endpoint = map.getOrDefault(request.method(), def);
        return endpoint != null ? endpoint.endpoint() : null;
    }

    @Override
    public @NotNull String describe() {
        StringBuilder builder = new StringBuilder("\n");
        for (SingleRouteEndpoint value : map.values()) {
            builder.append(" ").append(value.describe()).append("\n");
        }
        return builder.toString();
    }

    /*package*/ static @NotNull MultiRouteEndpoint fromEndpoints(@NotNull List<SingleRouteEndpoint> endpoints) {
        Map<HttpMethod, SingleRouteEndpoint> map = endpoints.stream().collect(Collectors.groupingBy(
            SingleRouteEndpoint::httpMethod,
            Collectors.reducing(null, (e1, e2) -> {
                if (e1 == null) {
                    return e2;
                }
                throw new UrlConfigError("Multiple endpoints on the same URL and HTTP method: %s and %s",
                                         e1.describe(), e2.describe());
            })
        ));
        return new MultiRouteEndpoint(map, map.get(null));
    }
}
