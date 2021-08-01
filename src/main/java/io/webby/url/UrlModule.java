package io.webby.url;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import io.routekit.Router;
import io.webby.url.caller.CallerFactory;
import io.webby.url.caller.ContentProviderFactory;
import io.webby.url.impl.HandlerScanner;
import io.webby.url.impl.RouteEndpoint;
import io.webby.url.impl.UrlBinder;
import io.webby.url.impl.UrlRouter;
import io.webby.url.view.RendererFactory;

public class UrlModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CallerFactory.class).asEagerSingleton();
        bind(ContentProviderFactory.class).asEagerSingleton();
        bind(HandlerScanner.class).asEagerSingleton();
        bind(RendererFactory.class).asEagerSingleton();
        bind(UrlBinder.class).asEagerSingleton();
        bind(UrlRouter.class).asEagerSingleton();
        bind(new TypeLiteral<Router<RouteEndpoint>>() {}).toProvider(UrlRouter.class);
    }
}
