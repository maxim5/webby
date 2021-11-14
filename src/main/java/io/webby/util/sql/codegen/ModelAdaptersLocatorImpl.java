package io.webby.util.sql.codegen;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.webby.common.ClasspathScanner;
import io.webby.util.sql.adapter.JdbcAdapt;
import io.webby.util.sql.schema.Naming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.regex.Pattern;

public class ModelAdaptersLocatorImpl implements ModelAdaptersLocator {
    private static final Pattern ADAPTER_NAME = Pattern.compile("\\w+JdbcAdapter");

    private final ImmutableMap<Class<?>, Class<?>> byClass;
    private final ImmutableMap<String, Class<?>> bySimpleName;

    @Inject
    public ModelAdaptersLocatorImpl(@NotNull ClasspathScanner scanner) {
        ImmutableMap.Builder<Class<?>, Class<?>> byClass = ImmutableMap.builder();
        ImmutableMap.Builder<String, Class<?>> bySimpleName = ImmutableMap.builder();
        Set<? extends Class<?>> adapters = scanner.getMatchingClasses(
            (pkg, klass) -> ADAPTER_NAME.matcher(klass).matches(),
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
                bySimpleName.put(defaultModelClassName(adapter.getSimpleName()), adapter);
            }
        }
        this.byClass = byClass.build();
        this.bySimpleName = bySimpleName.build();
    }

    @Override
    public @Nullable Class<?> locateAdapterClass(@NotNull Class<?> model) {
        return byClass.containsKey(model) ? byClass.get(model) : bySimpleName.get(Naming.generatedSimpleName(model));
    }

    @Override
    public @NotNull FQN locateAdapterFqn(@NotNull Class<?> model) {
        Class<?> klass = locateAdapterClass(model);
        if (klass != null) {
            return FQN.of(klass);
        }
        return new FQN(model.getPackageName(), defaultAdapterName(model));
    }

    public static @NotNull String defaultAdapterName(@NotNull Class<?> model) {
        if (model.isMemberClass()) {
            return "%s_JdbcAdapter".formatted(Naming.generatedSimpleName(model));
        }
        return "%sJdbcAdapter".formatted(model.getSimpleName());
    }

    public static @NotNull String defaultModelClassName(@NotNull String adapterName) {
        assert adapterName.endsWith("JdbcAdapter") : "Unexpected adapter name: %s".formatted(adapterName);
        return Naming.cutSuffix(adapterName, "JdbcAdapter");
    }
}
