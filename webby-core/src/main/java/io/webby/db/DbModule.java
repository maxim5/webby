package io.webby.db;

import com.google.inject.AbstractModule;
import io.webby.app.AppSettings;
import io.webby.common.InjectorHelper;
import io.webby.db.managed.BackgroundCacheCleaner;
import io.webby.db.codec.CodecProvider;
import io.webby.db.content.FileSystemStorage;
import io.webby.db.content.StableFingerprint;
import io.webby.db.content.UserContentStorage;
import io.webby.db.count.primitive.IntCounterFactory;
import io.webby.db.count.vote.VotingCounterFactory;
import io.webby.db.event.KeyEventStoreFactory;
import io.webby.db.kv.KeyValueFactory;
import io.webby.db.kv.etcd.EtcdSettings;
import io.webby.db.kv.impl.AgnosticKeyValueFactory;
import io.webby.db.kv.redis.RedisSettings;
import io.webby.db.sql.ConnectionPool;
import io.webby.db.sql.DDL;
import io.webby.db.sql.TableManager;
import io.webby.db.sql.tx.NoTxRunner;
import io.webby.db.sql.tx.TxRunner;
import io.webby.db.sql.tx.TxRunnerImpl;
import io.webby.orm.api.Connector;
import io.webby.orm.api.Engine;
import org.jetbrains.annotations.NotNull;

public class DbModule extends AbstractModule {
    private final AppSettings settings;

    public DbModule(@NotNull AppSettings settings) {
        this.settings = settings;
    }

    @Override
    protected void configure() {
        bind(StableFingerprint.class).asEagerSingleton();
        bind(UserContentStorage.class).to(FileSystemStorage.class).asEagerSingleton();

        bind(CodecProvider.class).asEagerSingleton();
        bind(AgnosticKeyValueFactory.class).asEagerSingleton();
        bind(KeyValueFactory.class).to(AgnosticKeyValueFactory.class).asEagerSingleton();

        bind(BackgroundCacheCleaner.class).asEagerSingleton();
        bind(VotingCounterFactory.class).asEagerSingleton();
        bind(IntCounterFactory.class).asEagerSingleton();
        bind(KeyEventStoreFactory.class).asEagerSingleton();

        bind(EtcdSettings.class).asEagerSingleton();
        bind(RedisSettings.class).asEagerSingleton();

        // SQL classes should better be lazy singletons
        // https://github.com/google/guice/issues/357
        if (settings.storageSettings().isSqlEnabled()) {
            bind(ConnectionPool.class).asEagerSingleton();
            bind(TableManager.class).asEagerSingleton();
            bind(DDL.class).asEagerSingleton();

            bind(Connector.class).toProvider(InjectorHelper.asProvider(TableManager.class, TableManager::connector)).asEagerSingleton();
            bind(Engine.class).toProvider(InjectorHelper.asProvider(TableManager.class, TableManager::engine)).asEagerSingleton();
            bind(TxRunner.class).to(TxRunnerImpl.class).asEagerSingleton();
        } else {
            bind(TxRunner.class).to(NoTxRunner.class);
        }

        bind(DbReadiness.class).asEagerSingleton();
    }
}
