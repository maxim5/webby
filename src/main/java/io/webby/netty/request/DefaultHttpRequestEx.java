package io.webby.netty.request;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.*;
import io.webby.url.convert.Constraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class DefaultHttpRequestEx extends DefaultFullHttpRequest implements MutableHttpRequestEx {
    private final AtomicReference<QueryParams> params = new AtomicReference<>(null);
    private final Map<String, Constraint<?>> constraints;
    private final Object[] attributes;

    public DefaultHttpRequestEx(@NotNull FullHttpRequest request,
                                @NotNull Map<String, Constraint<?>> constraints,
                                @NotNull Object[] attributes) {
        super(request.protocolVersion(), request.method(), request.uri(), request.content(),
              request.headers(), request.trailingHeaders());
        this.constraints = constraints;
        this.attributes = attributes;
    }

    public DefaultHttpRequestEx(@NotNull DefaultHttpRequestEx request) {
        this(request, request.constraints, request.attributes);
    }

    @Override
    public @NotNull Charset charset() {
        return HttpUtil.getCharset(this);
    }

    @Override
    public @NotNull String path() {
        return params().path();
    }

    @Override
    public @NotNull String query() {
        return params().query();
    }

    @Override
    public @NotNull QueryParams params() {
        return setIfAbsent(params, this::parseQuery);
    }

    @Override
    public <T> @NotNull T contentAsJson(@NotNull Class<T> klass) throws JsonParseException {
        InputStreamReader contentReader = new InputStreamReader(new ByteBufInputStream(content()), charset());
        return new Gson().fromJson(contentReader, klass);
    }

    @Override
    public @Nullable Object attr(int position) {
        return attributes[position];
    }

    @Override
    public void setAttr(int position, @NotNull Object attr) {
        assert attributes[position] == null : "Attributes can't be overwritten: %d".formatted(position);
        attributes[position] = attr;
    }

    @NotNull
    private QueryParams parseQuery() {
        QueryStringDecoder decoder = new QueryStringDecoder(uri(), charset());
        return QueryParams.fromDecoder(decoder, constraints);
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
