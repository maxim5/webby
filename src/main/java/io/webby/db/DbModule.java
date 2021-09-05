package io.webby.db;

import com.google.inject.AbstractModule;
import io.webby.db.kv.etcd.EtcdSettings;
import io.webby.db.kv.impl.AgnosticKeyValueFactory;
import io.webby.db.kv.KeyValueFactory;
import io.webby.db.codec.CodecProvider;
import io.webby.db.kv.redis.RedisSettings;

public class DbModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CodecProvider.class).asEagerSingleton();
        bind(AgnosticKeyValueFactory.class).asEagerSingleton();
        bind(KeyValueFactory.class).to(AgnosticKeyValueFactory.class).asEagerSingleton();

        bind(EtcdSettings.class).asEagerSingleton();
        bind(RedisSettings.class).asEagerSingleton();
    }
}
