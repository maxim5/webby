package io.webby.netty.intercept;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.webby.common.ClasspathScanner;
import io.webby.netty.intercept.attr.AttributeOwner;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;

import static io.webby.util.EasyCast.castAny;

public class InterceptorScanner {
    private static final BiPredicate<String, String> filter = (packageName, className) -> packageName.startsWith("io.webby");  // TODO: hard-coded

    @Inject private ClasspathScanner scanner;
    @Inject private Injector injector;

    @NotNull
    public List<InterceptItem> getInterceptorsFromClasspath() {
        return scanner.getDerivedClasses(filter, Interceptor.class)
                .stream()
                .map(klass -> {
                    Interceptor instance = castAny(injector.getInstance(klass));
                    boolean isOwner = klass.isAnnotationPresent(AttributeOwner.class);
                    int position = isOwner ? klass.getAnnotation(AttributeOwner.class).position() : Integer.MAX_VALUE;
                    boolean overridesEnable = Arrays.stream(klass.getDeclaredMethods())
                            .anyMatch(method -> method.getName().equals("isEnabled"));
                    return new InterceptItem(instance, isOwner, position, overridesEnable);
                }).sorted(Comparator.comparingInt(InterceptItem::position)).toList();
    }
}
