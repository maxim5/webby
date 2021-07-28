package io.webby.auth.session;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.webby.netty.exceptions.ServeException;
import io.webby.netty.intercept.Interceptor;
import io.webby.netty.intercept.attr.AttributeOwner;
import io.webby.netty.intercept.attr.Attributes;
import io.webby.netty.request.MutableHttpRequestEx;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.logging.Level;

@AttributeOwner(position = Attributes.Session)
public class SessionInterceptor implements Interceptor {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private static final String COOKIE_ID = "id";

    @Inject private SessionManager sessionManager;

    @Override
    public void enter(@NotNull MutableHttpRequestEx request) throws ServeException {
        List<Cookie> cookies = request.cookies();
        log.at(Level.INFO).log("Request cookies: %s", cookies);

        Cookie sessionCookie = cookies.stream()
                .filter(cookie -> cookie.name().equals(COOKIE_ID))
                .findFirst()
                .orElse(null);
        Session session = sessionManager.getOrCreateSession(sessionCookie);
        request.setAttr(Attributes.Session, session);
    }

    @Override
    public @NotNull FullHttpResponse exit(@NotNull MutableHttpRequestEx request, @NotNull FullHttpResponse response) {
        Session session = request.session();
        boolean shouldSetCookie = session.created().plusSeconds(60).isAfter(Instant.now());  // TODO: hack
        if (shouldSetCookie) {
            String cookieValue = sessionManager.encodeSession(session);
            Cookie cookie = new DefaultCookie(COOKIE_ID, cookieValue);
            cookie.setMaxAge(2592000);
            response.headers().set(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.LAX.encode(cookie));  // TODO: LAX
            log.at(Level.INFO).log("Response set-cookie: %s", ServerCookieEncoder.LAX.encode(cookie));
        }
        return Interceptor.super.exit(request, response);
    }
}
