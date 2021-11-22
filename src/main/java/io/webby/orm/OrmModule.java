package io.webby.orm;

import com.google.inject.AbstractModule;
import io.webby.orm.codegen.ModelAdaptersScanner;

public class OrmModule extends AbstractModule {
    public void configure() {
        bind(ModelAdaptersScanner.class).asEagerSingleton();
    }
}
