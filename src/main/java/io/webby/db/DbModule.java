package io.webby.db;

import com.google.inject.AbstractModule;
import io.webby.db.kv.AgnosticKeyValueFactory;
import io.webby.db.kv.KeyValueFactory;
import io.webby.db.codec.CodecProvider;

public class DbModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CodecProvider.class).asEagerSingleton();
        bind(KeyValueFactory.class).to(AgnosticKeyValueFactory.class).asEagerSingleton();
    }
}
