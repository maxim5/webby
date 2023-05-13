package io.webby.netty.request;

import com.google.inject.AbstractModule;

public class RequestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HttpRequestFactory.class).asEagerSingleton();
    }
}
