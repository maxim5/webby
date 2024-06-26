package io.spbx.webby.netty.request;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.spbx.util.lazy.AtomicCacheCompute;
import io.spbx.util.lazy.CacheCompute;
import io.spbx.webby.auth.CookieUtil;
import io.spbx.webby.auth.session.SessionModel;
import io.spbx.webby.auth.user.UserModel;
import io.spbx.webby.netty.HttpConst;
import io.spbx.webby.netty.intercept.attr.Attributes;
import io.spbx.webby.netty.marshal.Json;
import io.spbx.webby.url.convert.Constraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static io.spbx.util.base.EasyCast.castAny;

public class DefaultHttpRequestEx extends DefaultFullHttpRequest implements MutableHttpRequestEx {
    private final CacheCompute<QueryParams> paramsCache = AtomicCacheCompute.createEmpty();
    private final Channel channel;
    private final Json json;
    private final Map<String, Constraint<?>> constraints;
    private final Object[] attributes;

    public DefaultHttpRequestEx(@NotNull FullHttpRequest request,
                                @NotNull Channel channel,
                                @NotNull Json json,
                                @NotNull Map<String, Constraint<?>> constraints,
                                @Nullable Object @NotNull [] attributes) {
        super(request.protocolVersion(), request.method(), request.uri(), request.content(),
              request.headers(), request.trailingHeaders());
        this.channel = channel;
        this.json = json;
        this.constraints = constraints;
        this.attributes = attributes;
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
        return paramsCache.getOrCompute(this::parseQuery);
    }

    @Override
    public @NotNull String contentAsString() {
        return content().toString(charset());
    }

    @Override
    public <T> @NotNull T contentAsJson(@NotNull Class<T> klass) throws IllegalArgumentException {
        try {
            return json.withCustomCharset(charset()).readByteBuf(content().markReaderIndex(), klass);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Failed to parse JSON content", e);
        } finally {
            content().resetReaderIndex();
        }
    }

    public @NotNull DefaultHttpRequestEx withHeaders(@NotNull Map<CharSequence, Object> headers) {
        HttpHeaders requestHeaders = headers();
        headers.forEach(requestHeaders::add);
        return this;
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

    private void forceSetAttr(int position, @Nullable Object attr) {
        attributes[position] = attr;
    }

    @Override
    public @NotNull List<Cookie> cookies() {
        String cookieHeader = headers().get(HttpConst.COOKIE);
        return cookieHeader != null ? CookieUtil.decodeAll(cookieHeader) : List.of();
    }

    @Override
    public @NotNull <S extends SessionModel> S session() {
        return attrOrDie(Attributes.Session);
    }

    protected @Nullable SessionModel sessionOrNull() {
        return castAny(attr(Attributes.Session));
    }

    @Override
    public @Nullable <U extends UserModel> U user() {
        return castAny(attr(Attributes.User));
    }

    @Override
    public @NotNull <U extends UserModel> U userOrDie() {
        return attrOrDie(Attributes.User);
    }

    @Override
    public boolean isAuthenticated() {
        SessionModel session = sessionOrNull();
        return session != null && session.isAuthenticated();
    }

    @Override
    public void authenticate(@NotNull UserModel user) {
        SessionModel session = session();
        assert !session.isAuthenticated() : "Current session is already authenticated: " + session;
        assert user() == null : "Current session already has a user: " + user();
        forceSetAttr(Attributes.User, user);
        forceSetAttr(Attributes.Session, session.withUser(user));
    }

    @Override
    public void setSession(@NotNull SessionModel session) {
        assert user() == null : "Current session already has a user: " + user();
        setAttr(Attributes.Session, session);
    }

    @Override
    public void setUser(@NotNull UserModel user) {
        SessionModel session = session();
        assert session.isAuthenticated() && session.userId() == user.userId() :
            "Provided user is inconsistent with session: user=%s session=%s".formatted(user, session);
        setAttr(Attributes.User, user);
    }

    private @NotNull QueryParams parseQuery() {
        QueryStringDecoder decoder = new QueryStringDecoder(uri(), charset());
        return QueryParams.fromDecoder(decoder, constraints);
    }
}
