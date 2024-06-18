package io.webby.orm;

import com.google.inject.AbstractModule;
import io.spbx.orm.arch.factory.ArchJavaRunner;
import io.spbx.orm.codegen.ModelAdaptersLocator;

public class OrmModule extends AbstractModule {
    public void configure() {
        bind(ModelAdaptersLocator.class).to(ModelAdaptersScanner.class).asEagerSingleton();
        bind(ModelAdaptersScanner.class).asEagerSingleton();
        bind(ArchJavaRunner.class).asEagerSingleton();
    }
}
