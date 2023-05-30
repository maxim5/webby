package io.webby.orm;

import com.google.inject.AbstractModule;
import io.webby.orm.codegen.ModelAdaptersScanner;
import io.webby.orm.codegen.ModelAdaptersScannerImpl;

public class OrmModule extends AbstractModule {
    public void configure() {
        bind(ModelAdaptersScanner.class).to(ModelAdaptersScannerImpl.class).asEagerSingleton();
    }
}
