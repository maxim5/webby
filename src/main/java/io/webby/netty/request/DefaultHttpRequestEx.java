package io.webby.netty.request;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.webby.auth.CookieUtil;
import io.webby.auth.session.Session;
import io.webby.auth.user.User;
import io.webby.netty.intercept.attr.Attributes;
import io.webby.netty.marshal.Json;
import io.webby.url.convert.Constraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static io.webby.util.EasyCast.castAny;

public class DefaultHttpRequestEx extends DefaultFullHttpRequest implements MutableHttpRequestEx {
    private final AtomicReference<QueryParams> params = new AtomicReference<>(null);
    private final Channel channel;
    private final Json json;
    private final Map<String, Constraint<?>> constraints;
    private final Object[] attributes;

    public DefaultHttpRequestEx(@NotNull FullHttpRequest request,
                                @NotNull Channel channel,
                                @NotNull Json json,
                                @NotNull Map<String, Constraint<?>> constraints,
                                Object @NotNull [] attributes) {
        super(request.protocolVersion(), request.method(), request.uri(), request.content(),
              request.headers(), request.trailingHeaders());
        this.channel = channel;
        this.json = json;
        this.constraints = constraints;
        this.attributes = attributes;
    }

    public DefaultHttpRequestEx(@NotNull DefaultHttpRequestEx request) {
        this(request, request.channel, request.json, request.constraints, request.attributes);
    }

    @Override
    public @NotNull SocketAddress remoteAddress() {
        return channel.remoteAddress();
    }

    @Override
    public @Nullable InetAddress remoteInetAddress() {
        SocketAddress remoteAddress = remoteAddress();
        if (remoteAddress instanceof InetSocketAddress inetSocketAddress) {
            return inetSocketAddress.getAddress();
        }
        return null;
    }

    @Override
    public @Nullable String remoteIPAddress() {
        // See a more complex schema: https://stackoverflow.com/a/61861140/712995
        String forwarded = headers().get("X-Forwarded-For");
        if (forwarded != null) {
            return forwarded;
        }
        InetAddress inetAddress = remoteInetAddress();
        return inetAddress != null ? inetAddress.getHostAddress() : null;
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
    public <T> @NotNull T contentAsJson(@NotNull Class<T> klass) throws IllegalArgumentException {
        try {
            return json.withCustomCharset(charset()).readByteBuf(content(), klass);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Failed to parse JSON content", e);
        }
    }

    @Override
    public @Nullable Object attr(int position) {
        return attributes[position];
    }

    @Override
    public <T> @NotNull T attrOrDie(int position) {
        Object value = attr(position);
        assert value != null : "Attribute %d is expected to be set".formatted(position);
        return castAny(value);
    }

    @Override
    public void setAttr(int position, @NotNull Object attr) {
        assert attributes[position] == null : "Attributes can't be overwritten: %d".formatted(position);
        attributes[position] = attr;
    }

    @Override
    public void setNullableAttr(int position, @Nullable Object attr) {
        assert attributes[position] == null : "Attributes can't be overwritten: %d".formatted(position);
        attributes[position] = attr;
    }

    @Override
    public @NotNull List<Cookie> cookies() {
        String cookieHeader = headers().get(HttpHeaderNames.COOKIE);
        return cookieHeader != null ? CookieUtil.decodeAll(cookieHeader) : List.of();
    }

    @Override
    public @NotNull Session session() {
        return attrOrDie(Attributes.Session);
    }

    @Override
    public boolean isAuthenticated() {
        return session().hasUser();
    }

    @Override
    public @Nullable User user() {
        return (User) attr(Attributes.User);
    }

    @Override
    public @NotNull User userOrDie() {
        return attrOrDie(Attributes.User);
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
