package io.webby.url;

import com.google.inject.AbstractModule;

public class UrlModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HandlerFinder.class).asEagerSingleton();
        bind(UrlBinder.class).asEagerSingleton();
        bind(UrlRouter.class).asEagerSingleton();
    }
}
