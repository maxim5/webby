package io.webby.url;

import com.google.inject.AbstractModule;
import io.webby.app.AppSettings;
import io.webby.url.caller.CallerFactory;
import io.webby.url.caller.ContentProviderFactory;
import io.webby.url.impl.HandlerFinder;
import io.webby.url.impl.UrlBinder;
import io.webby.url.impl.UrlRouter;

public class UrlModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CallerFactory.class).asEagerSingleton();
        bind(ContentProviderFactory.class).asEagerSingleton();
        bind(HandlerFinder.class).asEagerSingleton();
        bind(UrlBinder.class).asEagerSingleton();
        bind(UrlRouter.class).asEagerSingleton();

        // TODO: move to AppModule
        bind(AppSettings.class).asEagerSingleton();
    }
}
