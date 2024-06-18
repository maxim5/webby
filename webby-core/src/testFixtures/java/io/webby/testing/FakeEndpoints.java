package io.webby.testing;

import io.netty.handler.codec.http.FullHttpRequest;
import io.spbx.util.base.CharArray;
import io.webby.url.caller.Caller;
import io.webby.url.impl.Endpoint;
import io.webby.url.impl.EndpointContext;
import io.webby.url.impl.EndpointOptions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FakeEndpoints {
    public static @NotNull Caller fakeCaller() {
        return new Caller() {
            @Override
            public Object call(@NotNull FullHttpRequest request, @NotNull Map<String, CharArray> variables) {
                throw new UnsupportedOperationException("A fake caller called with " + request);
            }
            @Override
            public @NotNull Object method() {
                throw new UnsupportedOperationException("A fake caller called to get a method");
            }
        };
    }

    public static @NotNull Endpoint fakeEndpoint() {
        return new Endpoint(fakeCaller(), fakeEndpointContext(), EndpointOptions.DEFAULT);
    }

    public static @NotNull EndpointContext fakeEndpointContext() {
        return new EndpointContext(Map.of(), false, false);
    }
}
