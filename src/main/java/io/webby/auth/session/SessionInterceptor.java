package io.webby.auth.session;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.webby.auth.CookieUtil;
import io.webby.netty.errors.ServeException;
import io.webby.netty.intercept.Interceptor;
import io.webby.netty.intercept.attr.AttributeOwner;
import io.webby.netty.intercept.attr.Attributes;
import io.webby.netty.request.MutableHttpRequestEx;
import io.webby.netty.response.ResponseHeaders;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Level;

@AttributeOwner(position = Attributes.Session)
public class SessionInterceptor implements Interceptor {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private static final String COOKIE_ID = "id";
    private static final int COOKIE_AGE = 2592000;

    @Inject private SessionManager sessionManager;

    @Override
    public void enter(@NotNull MutableHttpRequestEx request) throws ServeException {
        List<Cookie> cookies = request.cookies();
        log.at(Level.FINER).log("Request cookies: %s", cookies);

        Cookie sessionCookie = cookies.stream()
                .filter(cookie -> cookie.name().equals(COOKIE_ID))
                .findFirst()
                .orElse(null);
        Session session = sessionManager.getOrCreateSession(request, sessionCookie);
        request.setAttr(Attributes.Session, session);
    }

    @Override
    public @NotNull HttpResponse exit(@NotNull MutableHttpRequestEx request, @NotNull HttpResponse response) {
        Session session = request.session();
        if (session.shouldRefresh()) {
            String cookieValue = sessionManager.encodeSessionForCookie(session);
            Cookie cookie = new DefaultCookie(COOKIE_ID, cookieValue);
            cookie.setMaxAge(COOKIE_AGE);
            response.headers().set(ResponseHeaders.SET_COOKIE, CookieUtil.encode(cookie));
            log.at(Level.FINER).log("Response set-cookie: %s", cookie);
        }
        return response;
    }
}
