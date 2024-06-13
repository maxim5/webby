package io.webby.orm.codegen;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.webby.app.ClassFilter;
import io.webby.app.Settings;
import io.webby.app.AppClasspathScanner;
import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.arch.util.Naming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.regex.Pattern;

public class ModelAdaptersScannerImpl implements ModelAdaptersScanner {
    private static final Pattern ADAPTER_NAME = Pattern.compile("\\w+JdbcAdapter");
    private static final ClassFilter FILTER_BY_NAME = ClassFilter.of((pkg, cls) -> ADAPTER_NAME.matcher(cls).matches());

    private final ImmutableMap<Class<?>, Class<?>> byClass;
    private final ImmutableMap<String, Class<?>> bySimpleName;

    @Inject
    public ModelAdaptersScannerImpl(@NotNull Settings settings, @NotNull AppClasspathScanner scanner) {
        ImmutableMap.Builder<Class<?>, Class<?>> byClass = ImmutableMap.builder();
        ImmutableMap.Builder<String, Class<?>> bySimpleName = ImmutableMap.builder();
        Set<Class<?>> adapters = scanner
            .timed("model adapter")
            .scanToSet(ClassFilter.matchingAllOf(settings.modelFilter(), FILTER_BY_NAME));
        for (Class<?> adapter : adapters) {
            if (adapter.isAnnotationPresent(JdbcAdapt.class)) {
                JdbcAdapt annotation = adapter.getAnnotation(JdbcAdapt.class);
                for (Class<?> klass : annotation.value()) {
                    byClass.put(klass, adapter);
                }
            } else {
                bySimpleName.put(Naming.defaultModelClassName(adapter.getSimpleName()), adapter);
            }
        }
        this.byClass = byClass.build();
        this.bySimpleName = bySimpleName.build();
    }

    @Override
    public @Nullable Class<?> locateAdapterClass(@NotNull Class<?> model) {
        return byClass.containsKey(model) ? byClass.get(model) : bySimpleName.get(Naming.generatedSimpleJavaName(model));
    }
}
