package io.webby.orm.codegen;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.webby.app.ClassFilter;
import io.webby.app.Settings;
import io.webby.common.ClasspathScanner;
import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.arch.Naming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ModelAdaptersScanner {
    private static final Pattern ADAPTER_NAME = Pattern.compile("\\w+JdbcAdapter");
    private static final ClassFilter FILTER_BY_NAME = new ClassFilter((pkg, cls) -> ADAPTER_NAME.matcher(cls).matches());

    private final ImmutableMap<Class<?>, Class<?>> byClass;
    private final ImmutableMap<String, Class<?>> bySimpleName;

    @Inject
    public ModelAdaptersScanner(@NotNull Settings settings, @NotNull ClasspathScanner scanner) {
        ImmutableMap.Builder<Class<?>, Class<?>> byClass = ImmutableMap.builder();
        ImmutableMap.Builder<String, Class<?>> bySimpleName = ImmutableMap.builder();
        Set<? extends Class<?>> adapters = scanner.getMatchingClasses(
            ClassFilter.matchingAllOf(settings.modelFilter(), FILTER_BY_NAME),
            klass -> true,
            "model adapter"
        );
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

    protected ModelAdaptersScanner(@NotNull Map<Class<?>, Class<?>> byClass, @NotNull Map<String, Class<?>> bySimpleName) {
        this.byClass = ImmutableMap.copyOf(byClass);
        this.bySimpleName = ImmutableMap.copyOf(bySimpleName);
    }

    public @Nullable Class<?> locateAdapterClass(@NotNull Class<?> model) {
        return byClass.containsKey(model) ? byClass.get(model) : bySimpleName.get(Naming.generatedSimpleJavaName(model));
    }

    public @NotNull FQN locateAdapterFqn(@NotNull Class<?> model) {
        Class<?> klass = locateAdapterClass(model);
        if (klass != null) {
            return FQN.of(klass);
        }
        return new FQN(model.getPackageName(), Naming.defaultAdapterName(model));
    }
}
