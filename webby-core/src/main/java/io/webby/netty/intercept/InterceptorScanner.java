package io.webby.netty.intercept;

import com.google.common.collect.Streams;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.webby.app.ClassFilter;
import io.webby.app.Settings;
import io.webby.app.AppClasspathScanner;
import io.webby.app.Packages;
import io.webby.netty.intercept.attr.AttributeOwner;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static io.webby.util.base.EasyCast.castAny;

public class InterceptorScanner {
    private static final ClassFilter DEFAULTS_FILTER = ClassFilter.of(
        (pkg, cls) -> pkg.startsWith(Packages.INTERCEPTORS) && cls.contains("Interceptor")
    );

    @Inject private Settings settings;
    @Inject private AppClasspathScanner scanner;
    @Inject private Injector injector;

    public @NotNull List<InterceptItem> getInterceptorsFromClasspath() {
        Stream<Class<?>> custom = scanner
            .timed("custom Interceptor")
            .getDerivedClasses(settings.interceptorFilter(), Interceptor.class)
            .stream();

        boolean includeDefaultInterceptors = settings.getBoolProperty("interceptors.default.always.include", true);
        if (includeDefaultInterceptors) {
            Stream<Class<?>> defaults = scanner
                .timed("default Interceptor")
                .getDerivedClasses(DEFAULTS_FILTER, Interceptor.class)
                .stream();
            Stream<Class<?>> joint = Streams.concat(defaults, custom).distinct();
            return toItems(joint);
        } else {
            return toItems(custom);
        }
    }

    private @NotNull List<InterceptItem> toItems(@NotNull Stream<Class<?>> stream) {
        return stream
            .filter(klass -> !klass.isAnonymousClass())  // exclude the tests
            .map(klass -> {
                Interceptor instance = castAny(injector.getInstance(klass));
                return toItem(instance, klass);
            }).sorted(Comparator.comparingInt(InterceptItem::position)).toList();
    }

    protected static @NotNull InterceptItem toItem(@NotNull Interceptor instance, @NotNull Class<?> klass) {
        boolean isOwner = klass.isAnnotationPresent(AttributeOwner.class);
        int position = isOwner ? klass.getAnnotation(AttributeOwner.class).position() : Integer.MAX_VALUE;
        boolean overridesEnable = Arrays.stream(klass.getDeclaredMethods())
            .anyMatch(method -> method.getName().equals("isEnabled"));
        return new InterceptItem(instance, isOwner, position, overridesEnable);
    }
}
