package io.webby;

import com.google.common.flogger.FluentLogger;
import io.routekit.Match;
import io.routekit.Router;
import io.routekit.RouterSetup;

import java.util.logging.Level;

public class Main {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    public static void main(String[] args) {
        Router<String> router = new RouterSetup<String>()
                .add("/", "home")
                .add("/index", "index")
                .add("/user", "all_users")
                .add("/user/{id}", "user")
                .add("/post", "all_posts")
                .add("/post/{id}", "post")
                .add("/post/{id}/{slug}", "post")
                .build();

        String url = "/post/12345/java-microbenchmark-harness";
        Match<String> match = router.routeOrNull(url);
        log.at(Level.INFO).log("%s -> %s", url, match);
    }
}
