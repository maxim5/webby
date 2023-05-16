package io.webby.demo.hello;

import io.webby.auth.session.SessionModel;
import io.webby.auth.user.UserModel;
import io.webby.netty.intercept.attr.Attributes;
import io.webby.netty.request.HttpRequestEx;
import io.webby.perf.stats.Stat;
import io.webby.perf.stats.impl.LocalStatsHolder;
import io.webby.url.annotate.GET;
import org.jetbrains.annotations.NotNull;

public class RequestAttr {
    @GET(url = "/attr/get")
    public String attributes(@NotNull HttpRequestEx request) {
        Object stats = request.attr(Attributes.Stats);
        SessionModel session = request.session();
        UserModel user = request.user();
        return "stats:%s, session:%s, user:%s".formatted(
            stats != null ? stats.getClass().getSimpleName() : null,
            session.userAgent(),
            user != null ? user.userId() : null
        );
    }

    @GET(url = "/attr/stats")
    public String stats(@NotNull HttpRequestEx request) {
        LocalStatsHolder.getLocalStats().report(Stat.DB_GET.key(), 777, 100, null);
        return "ok";
    }
}
