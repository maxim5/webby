package io.webby.netty.request;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.webby.url.convert.Constraint;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class DefaultHttpRequestEx extends DefaultFullHttpRequest implements HttpRequestEx {
    private final AtomicReference<QueryParams> params = new AtomicReference<>(null);
    private final Map<String, Constraint<?>> constraints;

    public DefaultHttpRequestEx(@NotNull FullHttpRequest request, @NotNull Map<String, Constraint<?>> constraints) {
        super(request.protocolVersion(), request.method(), request.uri(), request.content(),
                request.headers(), request.trailingHeaders());
        this.constraints = constraints;
    }

    @NotNull
    public QueryParams params() {
        return setIfAbsent(params, this::parseQuery);
    }

    @NotNull
    private QueryParams parseQuery() {
        QueryStringDecoder decoder = new QueryStringDecoder(uri());
        return new QueryParams(decoder.path(), decoder.rawQuery(), decoder.parameters(), constraints);
    }

    @NotNull
    private static <T> T setIfAbsent(@NotNull AtomicReference<T> reference, @NotNull Supplier<T> supplier) {
        T value = reference.get();
        if (value == null) {
            value = supplier.get();
            if (!reference.compareAndSet(null, value)) {
                return reference.get();
            }
        }
        return value;
    }
}
