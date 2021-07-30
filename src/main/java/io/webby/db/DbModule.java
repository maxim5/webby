package io.webby.db;

import com.google.inject.AbstractModule;
import io.webby.db.kv.AgnosticKeyValueDbFactory;
import io.webby.db.kv.KeyValueDbFactory;
import io.webby.db.kv.SerializeProvider;

public class DbModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SerializeProvider.class).asEagerSingleton();
        bind(KeyValueDbFactory.class).to(AgnosticKeyValueDbFactory.class).asEagerSingleton();
    }
}
