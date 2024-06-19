package io.spbx.webby.common;

import com.google.inject.AbstractModule;

public class CommonModule extends AbstractModule {
    public void configure() {
        bind(InjectorHelper.class).asEagerSingleton();
    }
}
