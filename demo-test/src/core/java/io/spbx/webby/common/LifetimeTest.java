package io.spbx.webby.common;

import com.google.common.collect.Lists;
import io.spbx.util.io.EasyIo;
import io.spbx.webby.app.AppSettings;
import io.spbx.webby.app.ClassFilter;
import io.spbx.webby.db.managed.BackgroundCacheCleaner;
import io.spbx.webby.db.sql.ConnectionPool;
import io.spbx.webby.db.sql.SqlSettings;
import io.spbx.webby.db.sql.TableManager;
import io.spbx.webby.testing.Testing;
import io.spbx.webby.testing.TestingModules;
import io.spbx.webby.testing.TestingStorage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class LifetimeTest {
    private final List<Class<?>> addedResources = new ArrayList<>();
    private final List<Class<?>> deconstructedResources = new ArrayList<>();

    @Test
    public void dependency_order() throws Throwable {
        AppSettings settings = Testing.defaultAppSettings();
        settings.setString("testing.logging", "io.spbx.webby.common.Lifetime.Definition=DEBUG");
        settings.setModelFilter(ClassFilter.ofSelectedPackagesOnly(Testing.CORE_MODELS));
        settings.updateStorageSettings(
            storage -> storage
                .withKeyValue(TestingStorage.KEY_VALUE_DEFAULT)
                .enableSql(SqlSettings.SQLITE_IN_MEMORY)
        );

        Lifetime.Definition lifetimeMock = mockLifetime();
        Testing.testStartup(settings, TestingModules.instance(Lifetime.class, lifetimeMock));
        lifetimeMock.terminate();

        List<Class<?>> expected = List.of(
            AppSettings.class,
            EasyIo.class,
            ConnectionPool.class,
            TableManager.class,
            BackgroundCacheCleaner.class
        );
        assertThat(addedResources).isEqualTo(expected);
        assertThat(deconstructedResources).isEqualTo(Lists.reverse(expected));
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
