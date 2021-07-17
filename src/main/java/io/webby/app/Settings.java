package io.webby.app;

import io.routekit.QueryParser;

import java.util.function.Predicate;

public interface Settings {
    boolean isDevMode();

    String webPath();

    Predicate<String> packageTester();

    QueryParser urlParser();
}
