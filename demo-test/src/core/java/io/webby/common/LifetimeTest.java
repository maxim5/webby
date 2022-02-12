package io.webby.common;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import io.webby.app.AppSettings;
import io.webby.db.cache.BackgroundCacheCleaner;
import io.webby.db.kv.KeyValueSettings;
import io.webby.db.kv.javamap.JavaMapDbFactory;
import io.webby.db.sql.SqlSettings;
import io.webby.db.sql.TableManager;
import io.webby.testing.Testing;
import io.webby.testing.TestingModules;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LifetimeTest {
    private final List<Class<?>> addedResources = new ArrayList<>();
    private final List<Class<?>> deconstructedResources = new ArrayList<>();

    @Test
    public void dependency_order() throws Throwable {
        AppSettings settings = Testing.defaultAppSettings();
        settings.setProperty("testing.logging", "io.webby.common.Lifetime.Definition=DEBUG");
        settings.modelFilter().setPackagesOf(Testing.CORE_MODELS);
        settings.storageSettings()
                .enableKeyValue(KeyValueSettings.of(KeyValueSettings.DEFAULT_TYPE))
                .enableSql(SqlSettings.SQLITE_IN_MEMORY);

        Lifetime.Definition lifetimeMock = mockLifetime();
        Testing.testStartup(settings, TestingModules.instance(Lifetime.class, lifetimeMock));
        lifetimeMock.terminate();

        List<Class<?>> expected = List.of(
            HikariDataSource.class,
            TableManager.class,
            JavaMapDbFactory.class,
            BackgroundCacheCleaner.class
        );
        assertEquals(expected, addedResources);
        assertEquals(Lists.reverse(expected), deconstructedResources);
    }

    private @NotNull Lifetime.Definition mockLifetime() throws Throwable {
        Lifetime.Definition lifetimeMock = Mockito.spy(Lifetime.Definition.class);
        Mockito.doAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            addedResources.add(arg.getClass().getNestHost());
            return invocation.callRealMethod();
        }).when(lifetimeMock).onTerminate(Mockito.any());

        Mockito.doAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            deconstructedResources.add(arg.getClass().getNestHost());
            return invocation.callRealMethod();
        }).when(lifetimeMock).deconstruct(Mockito.any());
        return lifetimeMock;
    }
}
