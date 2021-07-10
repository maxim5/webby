package io.webby.url;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.routekit.*;
import io.webby.url.caller.Caller;
import io.webby.url.caller.CallerFactory;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class UrlBinder {
    @Inject private HandlerFinder finder;
    @Inject private Injector injector;
    @Inject private CallerFactory callerFactory;

    public Router<Caller> bindRouter() {
        ArrayList<Binding> bindings = bindHandlersFromClasspath();
        return buildRouter(bindings);
    }

    @NotNull
    private ArrayList<Binding> bindHandlersFromClasspath() {
        ArrayList<Binding> bindings = new ArrayList<>();
        try {
            finder.getHandlerClasses().forEach(klass -> {
                for (Method method : klass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GET.class)) {
                        GET get = method.getAnnotation(GET.class);
                        String url = get.url();
                        method.setAccessible(true);
                        Binding binding = new Binding(url, method, get);
                        bindings.add(binding);
                    }
                }
            });
        } catch (Exception e) {
            throw new UrlConfigError(e);
        }
        return bindings;
    }

    private Router<Caller> buildRouter(@NotNull Collection<Binding> bindings) {
        SimpleQueryParser parser = SimpleQueryParser.DEFAULT;
        RouterSetup<Caller> setup = new RouterSetup<>();
        try {
            bindings.forEach(binding -> {
                Class<?> klass = binding.method.getDeclaringClass();
                Object instance = injector.getInstance(klass);

                List<String> vars = parser.parse(binding.url)
                        .stream()
                        .map(token -> token instanceof Variable var ? var.name() : null)
                        .filter(Objects::nonNull)
                        .toList();

                Caller caller = callerFactory.create(instance, binding.method(), binding.annotation, vars);
                setup.add(binding.url, caller);
            });
        } catch (QueryParseException e) {
            throw new UrlConfigError(e);
        }
        return setup.withParser(parser).build();
    }

    private record Binding(String url, Method method, Annotation annotation) {}
}
