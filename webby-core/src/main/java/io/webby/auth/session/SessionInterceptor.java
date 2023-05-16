package io.webby.auth.session;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.webby.auth.CookieUtil;
import io.webby.netty.HttpConst;
import io.webby.netty.errors.ServeException;
import io.webby.netty.intercept.Interceptor;
import io.webby.netty.intercept.attr.AttributeOwner;
import io.webby.netty.intercept.attr.Attributes;
import io.webby.netty.request.MutableHttpRequestEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@AttributeOwner(position = Attributes.Session)
public class SessionInterceptor implements Interceptor {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    private static final String COOKIE_ID = "id";
    private static final int COOKIE_AGE = 2592000;

    private static final long JUST_CREATED_MILLIS = TimeUnit.SECONDS.toMillis(60);
    private static final long TIME_TO_REFRESH_MILLIS = TimeUnit.DAYS.toMillis(30);

    @Inject private SessionManager sessionManager;

    @Override
    public void enter(@NotNull MutableHttpRequestEx request) throws ServeException {
        List<Cookie> cookies = request.cookies();
        log.at(Level.FINER).log("Request cookies: %s", cookies);

        Cookie sessionCookie = cookies.stream()
                .filter(cookie -> cookie.name().equals(COOKIE_ID))
                .findFirst()
                .orElse(null);
        SessionModel session = sessionManager.getOrCreateSession(request, sessionCookie);
        request.setAttr(Attributes.Session, session);
    }

    @Override
    public @NotNull HttpResponse exit(@NotNull MutableHttpRequestEx request, @NotNull HttpResponse response) {
        SessionModel session = request.session();
        if (shouldRefresh(session)) {
            String cookieValue = sessionManager.encodeSessionForCookie(session);
            Cookie cookie = new DefaultCookie(COOKIE_ID, cookieValue);
            cookie.setMaxAge(COOKIE_AGE);
            response.headers().set(HttpConst.SET_COOKIE, CookieUtil.encode(cookie));
            log.at(Level.FINER).log("Response set-cookie: %s", cookie);
        }
        return response;
    }

    @VisibleForTesting
    static boolean shouldRefresh(@NotNull SessionModel session) {
        long createdMillis = session.createdAt().getEpochSecond() * 1000;
        long now = System.currentTimeMillis();
        return createdMillis + JUST_CREATED_MILLIS >= now || createdMillis + TIME_TO_REFRESH_MILLIS < now;
    }
}
