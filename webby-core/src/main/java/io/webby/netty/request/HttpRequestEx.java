package io.webby.netty.request;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import io.webby.auth.session.Session;
import io.webby.auth.user.UserModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.List;

public interface HttpRequestEx extends FullHttpRequest {
    @NotNull SocketAddress remoteAddress();

    @Nullable InetAddress remoteInetAddress();

    @Nullable String remoteIPAddress();

    @NotNull Charset charset();

    @NotNull String path();

    @NotNull String query();

    @NotNull QueryParams params();

    @NotNull String contentAsString();

    <T> @NotNull T contentAsJson(@NotNull Class<T> klass) throws IllegalArgumentException;

    /**
     * Returns the attribute at a given {@code position} or null if not set.
     * The attributes are usually set by the interceptors (session, user, debug or performance information, etc).
     *
     * @see #attrOrDie(int)
     */
    @Nullable Object attr(int position);

    /**
     * Returns the attribute at a given {@code position} or throws if not set or the type doesn't match.
     * The attributes are usually set by the interceptors (session, user, debug or performance information, etc).
     * <p>
     * This method generally should be used only by the attribute owner, i.e. by the same interceptor which sets it.
     *
     * @see #attr(int)
     */
    <T> @NotNull T attrOrDie(int position);

    @NotNull List<Cookie> cookies();

    @NotNull Session session();

    boolean isAuthenticated();

    @Nullable <U extends UserModel> U user();

    @NotNull <U extends UserModel> U userOrDie();
}
