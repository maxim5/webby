package io.webby.netty.response;

import com.google.inject.AbstractModule;

public class ResponseModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ContentTypeDetector.class).asEagerSingleton();
        bind(HttpResponseFactory.class).asEagerSingleton();
        bind(ResponseHeaders.class).asEagerSingleton();
        bind(ResponseMapper.class).asEagerSingleton();
        bind(HttpCachingRequestProcessor.class).asEagerSingleton();
        bind(StaticServing.class).asEagerSingleton();
        bind(UserContentServing.class).asEagerSingleton();
    }
}
