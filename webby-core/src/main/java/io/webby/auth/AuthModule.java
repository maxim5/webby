package io.webby.auth;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.webby.app.AppSettings;
import io.webby.auth.session.*;
import io.webby.auth.user.*;
import org.jetbrains.annotations.NotNull;

public class AuthModule extends AbstractModule {
    private final AppSettings settings;

    public AuthModule(@NotNull AppSettings settings) {
        this.settings = settings;
    }

    @Override
    protected void configure() {
        boolean isSqlEnabled = settings.storageSettings().isSqlEnabled();

        bind(AuthInterceptor.class).asEagerSingleton();
        bind(SessionInterceptor.class).asEagerSingleton();

        bind(SessionStore.class).to(isSqlEnabled ? SqlSessionStore.class : KeyValueSessionStore.class).asEagerSingleton();
        bind(SessionManager.Factory.class).toInstance(DefaultSession::newSessionData);
        bind(SessionManager.class).asEagerSingleton();

        bind(UserStore.class).to(isSqlEnabled ? SqlUserStore.class : KeyValueUserStore.class).asEagerSingleton();
    }

    // TODO: detect user/session class automatically from settings/model setup?

    @Provides
    public Class<? extends UserModel> userClass() {
        return DefaultUser.class;
    }

    @Provides
    public Class<? extends SessionModel> sessionClass() {
        return DefaultSession.class;
    }
}
