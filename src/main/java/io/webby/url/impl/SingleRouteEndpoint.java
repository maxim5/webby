package io.webby.url.impl;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.webby.url.caller.Caller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

record SingleRouteEndpoint(EndpointCaller caller, @Nullable HttpMethod httpMethod) implements RouteEndpoint {
    @Override
    @Nullable
    public EndpointCaller getAcceptedCallerOrNull(@NotNull HttpRequest request) {
        return (httpMethod == null || httpMethod.equals(request.method())) ? caller : null;
    }

    /*package*/ static SingleRouteEndpoint fromBinding(@NotNull Binding binding, @NotNull Caller caller) {
        return new SingleRouteEndpoint(
                new EndpointCaller(caller, binding.options()),
                HttpMethod.valueOf(binding.type())
        );
    }
}
