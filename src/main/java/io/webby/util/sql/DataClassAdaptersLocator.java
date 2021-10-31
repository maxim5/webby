package io.webby.util.sql;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.webby.common.ClasspathScanner;
import io.webby.util.sql.adapter.JdbcAdapt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.regex.Pattern;

public class DataClassAdaptersLocator {
    private static final Pattern ADAPTER_NAME = Pattern.compile("\\w+JdbcAdapter");

    private final ImmutableMap<Class<?>, Class<?>> byClass;
    private final ImmutableMap<String, Class<?>> bySimpleName;

    @Inject
    public DataClassAdaptersLocator(@NotNull ClasspathScanner scanner) {
        ImmutableMap.Builder<Class<?>, Class<?>> byClass = ImmutableMap.builder();
        ImmutableMap.Builder<String, Class<?>> bySimpleName = ImmutableMap.builder();
        Set<? extends Class<?>> adapters = scanner.getMatchingClasses(
            (pkg, klass) -> ADAPTER_NAME.matcher(klass).matches(),
            klass -> true,
            "data adapter"
        );
        for (Class<?> adapter : adapters) {
            if (adapter.isAnnotationPresent(JdbcAdapt.class)) {
                JdbcAdapt annotation = adapter.getAnnotation(JdbcAdapt.class);
                for (Class<?> klass : annotation.value()) {
                    byClass.put(klass, adapter);
                }
            } else {
                bySimpleName.put(defaultDataClassName(adapter.getSimpleName()), adapter);
            }
        }
        this.byClass = byClass.build();
        this.bySimpleName = bySimpleName.build();
    }

    public @Nullable Class<?> locateAdapterClass(@NotNull Class<?> dataClass) {
        return byClass.containsKey(dataClass) ? byClass.get(dataClass) : bySimpleName.get(dataClass.getSimpleName());
    }

    public @NotNull FQN locateAdapterFqn(@NotNull Class<?> dataClass) {
        Class<?> klass = locateAdapterClass(dataClass);
        if (klass != null) {
            return FQN.of(klass);
        }
        return new FQN(dataClass.getPackageName(), defaultAdapterName(dataClass));
    }

    public static @NotNull String defaultAdapterName(@NotNull Class<?> klass) {
        return "%sJdbcAdapter".formatted(klass.getSimpleName());
    }

    public static @NotNull String defaultDataClassName(@NotNull String adapterName) {
        return adapterName.substring(0, adapterName.length() - "JdbcAdapter".length());
    }
}
