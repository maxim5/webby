package io.webby.orm;

import com.google.inject.AbstractModule;
import io.webby.orm.arch.factory.ArchJavaRunner;
import io.webby.orm.codegen.AppModelAdaptersScanner;
import io.webby.orm.codegen.ModelAdaptersLocator;

public class OrmModule extends AbstractModule {
    public void configure() {
        bind(ModelAdaptersLocator.class).to(AppModelAdaptersScanner.class).asEagerSingleton();
        bind(AppModelAdaptersScanner.class).asEagerSingleton();
        bind(ArchJavaRunner.class).asEagerSingleton();
    }
}
