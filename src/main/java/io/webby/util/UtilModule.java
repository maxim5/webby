package io.webby.util;

import com.google.inject.AbstractModule;
import io.webby.util.sql.codegen.ModelAdaptersLocator;
import io.webby.util.sql.codegen.ModelAdaptersLocatorImpl;

public class UtilModule extends AbstractModule {
    public void configure() {
        bind(ModelAdaptersLocator.class).to(ModelAdaptersLocatorImpl.class).asEagerSingleton();
    }
}
