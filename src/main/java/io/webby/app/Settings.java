package io.webby.app;

import io.routekit.QueryParser;
import io.webby.url.annotate.Marshal;
import io.webby.url.annotate.Render;

import java.util.function.BiPredicate;

public interface Settings {
    boolean isDevMode();

    boolean isHotReload();

    String webPath();

    String viewPath();

    BiPredicate<String, String> filter();

    QueryParser urlParser();

    Marshal defaultRequestContentMarshal();

    Marshal defaultResponseContentMarshal();

    Render defaultRender();
}
