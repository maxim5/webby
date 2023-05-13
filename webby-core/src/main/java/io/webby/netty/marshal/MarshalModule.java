package io.webby.netty.marshal;

import com.google.inject.AbstractModule;

public class MarshalModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MarshallerFactory.class).asEagerSingleton();
        bind(Json.class).toProvider(MarshallerFactory.class).asEagerSingleton();
    }
}
