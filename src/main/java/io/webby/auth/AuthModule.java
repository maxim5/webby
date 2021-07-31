package io.webby.auth;

import com.google.inject.AbstractModule;
import io.webby.auth.session.SessionInterceptor;
import io.webby.auth.session.SessionManager;
import io.webby.auth.user.DefaultUserFactory;
import io.webby.auth.user.UserFactory;
import io.webby.auth.user.UserManager;

public class AuthModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AuthInterceptor.class).asEagerSingleton();
        bind(SessionInterceptor.class).asEagerSingleton();
        bind(SessionManager.class).asEagerSingleton();

        bind(UserManager.class).asEagerSingleton();
        bind(UserFactory.class).to(DefaultUserFactory.class).asEagerSingleton();
    }
}
