package io.spbx.webby.url;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import io.spbx.webby.routekit.Router;
import io.spbx.webby.url.caller.CallerFactory;
import io.spbx.webby.url.caller.ContentProviderFactory;
import io.spbx.webby.url.impl.HandlerBinder;
import io.spbx.webby.url.impl.HandlerScanner;
import io.spbx.webby.url.impl.RouteEndpoint;
import io.spbx.webby.url.impl.UrlRouter;
import io.spbx.webby.url.view.ManualRenderer;
import io.spbx.webby.url.view.RendererFactory;

public class UrlModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CallerFactory.class).asEagerSingleton();
        bind(ContentProviderFactory.class).asEagerSingleton();
        bind(RendererFactory.class).asEagerSingleton();
        bind(ManualRenderer.class).asEagerSingleton();

        bind(HandlerBinder.class).asEagerSingleton();
        bind(HandlerScanner.class).asEagerSingleton();

        bind(UrlRouter.class).asEagerSingleton();
        bind(new TypeLiteral<Router<RouteEndpoint>>() {}).toProvider(UrlRouter.class);
    }
}
