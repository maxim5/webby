package io.spbx.webby.db;

import com.google.inject.AbstractModule;
import io.spbx.orm.api.Connector;
import io.spbx.orm.api.Engine;
import io.spbx.webby.app.AppSettings;
import io.spbx.webby.common.InjectorHelper;
import io.spbx.webby.db.codec.CodecProvider;
import io.spbx.webby.db.content.FileSystemStorage;
import io.spbx.webby.db.content.StableFingerprint;
import io.spbx.webby.db.content.UserContentStorage;
import io.spbx.webby.db.count.primitive.IntCounterFactory;
import io.spbx.webby.db.count.vote.VotingCounterFactory;
import io.spbx.webby.db.event.KeyEventStoreFactory;
import io.spbx.webby.db.kv.KeyValueFactory;
import io.spbx.webby.db.kv.etcd.EtcdSettings;
import io.spbx.webby.db.kv.impl.AgnosticKeyValueFactory;
import io.spbx.webby.db.kv.redis.RedisSettings;
import io.spbx.webby.db.managed.BackgroundCacheCleaner;
import io.spbx.webby.db.sql.ConnectionPool;
import io.spbx.webby.db.sql.DDL;
import io.spbx.webby.db.sql.TableManager;
import io.spbx.webby.db.sql.tx.NoTxRunner;
import io.spbx.webby.db.sql.tx.TxRunner;
import io.spbx.webby.db.sql.tx.TxRunnerImpl;
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
