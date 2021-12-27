package io.webby.auth;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.webby.app.AppSettings;
import io.webby.auth.session.SessionInterceptor;
import io.webby.auth.session.SessionManager;
import io.webby.auth.user.*;
import org.jetbrains.annotations.NotNull;

public class AuthModule extends AbstractModule {
    private final AppSettings settings;

    public AuthModule(@NotNull AppSettings settings) {
        this.settings = settings;
    }

    @Override
    protected void configure() {
        bind(AuthInterceptor.class).asEagerSingleton();
        bind(SessionInterceptor.class).asEagerSingleton();
        bind(SessionManager.class).asEagerSingleton();

        boolean isSqlEnabled = settings.storageSettings().isSqlEnabled();
        bind(UserManager.class).asEagerSingleton();
        bind(UserStorage.class).to(isSqlEnabled ? SqlUserStorage.class : KeyValueUserStorage.class).asEagerSingleton();
    }

    @Provides
    public Class<? extends User> userClass() {
        return DefaultUser.class;
    }
}
