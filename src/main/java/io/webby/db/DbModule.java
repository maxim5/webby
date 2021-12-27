package io.webby.db;

import com.google.inject.AbstractModule;
import io.webby.db.codec.CodecProvider;
import io.webby.db.content.FileSystemStorage;
import io.webby.db.content.StableFingerprint;
import io.webby.db.content.UserContentStorage;
import io.webby.db.event.KeyCountersFactory;
import io.webby.db.event.KeyEventStoreFactory;
import io.webby.db.kv.KeyValueFactory;
import io.webby.db.kv.etcd.EtcdSettings;
import io.webby.db.kv.impl.AgnosticKeyValueFactory;
import io.webby.db.kv.redis.RedisSettings;

public class DbModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(StableFingerprint.class).asEagerSingleton();
        bind(UserContentStorage.class).to(FileSystemStorage.class).asEagerSingleton();

        bind(CodecProvider.class).asEagerSingleton();
        bind(AgnosticKeyValueFactory.class).asEagerSingleton();
        bind(KeyValueFactory.class).to(AgnosticKeyValueFactory.class).asEagerSingleton();

        bind(KeyCountersFactory.class).asEagerSingleton();
        bind(KeyEventStoreFactory.class).asEagerSingleton();

        bind(EtcdSettings.class).asEagerSingleton();
        bind(RedisSettings.class).asEagerSingleton();

        // SQL classes must be lazy singletons
        // https://github.com/google/guice/issues/357
        // SqlDbModule?
    }
}
