package io.webby.util;

import com.google.inject.AbstractModule;
import io.webby.util.sql.codegen.ModelAdaptersScanner;
import io.webby.util.sql.codegen.ModelAdaptersScannerImpl;

public class UtilModule extends AbstractModule {
    public void configure() {
        bind(ModelAdaptersScanner.class).to(ModelAdaptersScannerImpl.class).asEagerSingleton();
    }
}
