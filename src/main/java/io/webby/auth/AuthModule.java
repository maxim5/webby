package io.webby.auth;

import com.google.inject.AbstractModule;
import io.webby.auth.session.SessionInterceptor;
import io.webby.auth.session.SessionManager;

public class AuthModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AuthInterceptor.class).asEagerSingleton();
        bind(SessionInterceptor.class).asEagerSingleton();
        bind(SessionManager.class).asEagerSingleton();
    }
}
