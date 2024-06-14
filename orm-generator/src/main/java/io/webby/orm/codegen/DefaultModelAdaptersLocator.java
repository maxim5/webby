package io.webby.orm.codegen;

import com.google.common.collect.ImmutableMap;
import io.webby.orm.adapter.JdbcAdapt;
import io.webby.orm.arch.util.Naming;
import io.webby.util.classpath.ClassNamePredicate;
import io.webby.util.classpath.GuavaClasspathScanner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.regex.Pattern;

public class DefaultModelAdaptersLocator implements ModelAdaptersLocator {
    protected static final Pattern ADAPTER_NAME = Pattern.compile("\\w+JdbcAdapter");
    protected static final ClassNamePredicate FILTER_BY_NAME = (pkg, cls) -> ADAPTER_NAME.matcher(cls).matches();
    protected static final String NAME = "model adapter";

    private final ImmutableMap<Class<?>, Class<?>> byClass;
    private final ImmutableMap<String, Class<?>> bySimpleName;

    public DefaultModelAdaptersLocator() {
        this(GuavaClasspathScanner.fromSystemClassLoader().timed(NAME).scanToSet(FILTER_BY_NAME));
    }

    protected DefaultModelAdaptersLocator(@NotNull Set<Class<?>> adapters) {
        ImmutableMap.Builder<Class<?>, Class<?>> byClass = ImmutableMap.builder();
        ImmutableMap.Builder<String, Class<?>> bySimpleName = ImmutableMap.builder();
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
