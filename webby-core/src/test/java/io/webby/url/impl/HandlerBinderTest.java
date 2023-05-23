package io.webby.url.impl;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.routekit.SimpleQueryParser;
import io.webby.testing.Testing;
import io.webby.url.annotate.GET;
import io.webby.url.caller.Caller;
import io.webby.util.collect.Pair;
import io.webby.util.reflect.EasyMembers.Scope;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import static com.google.common.truth.Truth.assertThat;
import static io.webby.util.reflect.EasyMembers.findMethod;
import static java.util.Objects.requireNonNull;

public class HandlerBinderTest {
    @Test
    public void getEndpoints_simple_handler() throws Exception {
        HandlerBinder binder = Testing.testStartup().getInstance(HandlerBinder.class);
        Map<String, RouteEndpoint> endpoints = getEndpoints(binder, SimpleHandler.class);

        SingleRouteEndpoint foo = assertSingleRoute(endpoints.get("/foo"), HttpMethod.GET, true);
        Object instanceFoo = assertCaller(foo.endpoint().caller(), "SimpleHandler.foo");

        SingleRouteEndpoint bar = assertSingleRoute(endpoints.get("/bar"), HttpMethod.GET, false);
        Object instanceBar = assertCaller(bar.endpoint().caller(), "SimpleHandler.bar");

        assertThat(instanceFoo).isSameInstanceAs(instanceBar);
    }

    private static @NotNull SingleRouteEndpoint
            assertSingleRoute(@NotNull RouteEndpoint endpoint, @NotNull HttpMethod method, boolean isVoid) {
        assertThat(endpoint instanceof SingleRouteEndpoint).isTrue();
        SingleRouteEndpoint single = (SingleRouteEndpoint) endpoint;
        assertThat(single.httpMethod()).isEqualTo(method);
        assertThat(single.endpoint().context().isVoid()).isEqualTo(isVoid);
        return single;
    }

    private static @NotNull Object assertCaller(@NotNull Caller caller, @NotNull String method) throws Exception {
        Pair<String, String> pair = Pair.of(method.split("\\."));
        assertThat(((Method) caller.method()).getName()).isEqualTo(pair.second());
        Object instance = requireNonNull(findMethod(caller.getClass(), Scope.DECLARED, "instance")).invoke(caller);
        assertThat(instance.getClass().getSimpleName()).isEqualTo(pair.first());
        return instance;
    }

    private static @NotNull Map<String, RouteEndpoint> getEndpoints(@NotNull HandlerBinder binder,
                                                                    @NotNull Class<?> handlerClass) {
        Map<String, RouteEndpoint> endpoints = new HashMap<>();
        List<Binding> bindings = binder.getBindings(Set.of(handlerClass));
        binder.processBindings(bindings, SimpleQueryParser.DEFAULT, endpoints::put);
        return endpoints;
    }

    public static class SimpleHandler {
        @GET(url = "/foo")
        public void foo() {}

        @GET(url = "/bar")
        public int bar(@NotNull HttpRequest request) { return 0; }
    }

    private static @NotNull BiPredicate<String, String> onlyInsideClass(@NotNull Class<?> klass) {
        String name = klass.getName();
        int lastDot = name.lastIndexOf('.');
        assert lastDot > 0 : name;
        String thisPackage = name.substring(0, lastDot);
        String thisClassName = name.substring(lastDot + 1);
        return (pkg, cls) -> pkg.equals(thisPackage);
    }
}
