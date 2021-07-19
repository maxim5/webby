package io.webby.app;

import io.routekit.QueryParser;
import io.webby.url.annotate.Marshal;

import java.util.function.BiPredicate;

public interface Settings {
    boolean isDevMode();

    String webPath();

    BiPredicate<String, String> filter();

    QueryParser urlParser();

    Marshal defaultRequestContentMarshal();

    Marshal defaultResponseContentMarshal();
}
