package io.webby.common;

import com.google.inject.AbstractModule;

public class CommonModule extends AbstractModule {
    public void configure() {
        bind(ClasspathScanner.class).asEagerSingleton();
    }
}
