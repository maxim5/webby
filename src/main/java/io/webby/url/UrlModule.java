package io.webby.url;

import com.google.inject.AbstractModule;
import io.webby.url.impl.HandlerFinder;
import io.webby.url.impl.UrlBinder;
import io.webby.url.impl.UrlRouter;

public class UrlModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HandlerFinder.class).asEagerSingleton();
        bind(UrlBinder.class).asEagerSingleton();
        bind(UrlRouter.class).asEagerSingleton();
    }
}
