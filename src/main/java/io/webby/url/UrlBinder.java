package io.webby.url;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.routekit.Router;
import io.routekit.RouterSetup;
import io.routekit.util.CharBuffer;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

public class UrlBinder {
    @Inject private HandlerFinder finder;
    @Inject private Injector injector;

    public Router<Caller> bindRouter() {
        ArrayList<Binding> bindings = bindHandlers();

        RouterSetup<Caller> setup = new RouterSetup<>();
        bindings.forEach(binding -> {
            Class<?> klass = binding.method.getDeclaringClass();
            Object instance = injector.getInstance(klass);
            Caller caller = new Caller(instance, binding.method());
            setup.add(binding.url, caller);
        });
        return setup.build();
    }

    @NotNull
    private ArrayList<Binding> bindHandlers() {
        ArrayList<Binding> bindings = new ArrayList<>();
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
        return bindings;
    }

    private record Binding(String url, Method method, Annotation annotation) {
    }

    public record Caller(Object instance, Method method) {
        public Object call(@NotNull CharSequence url, @NotNull Map<String, CharBuffer> variables) {
            try {
                return method.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to call handler for url: " + url, e);
            }
        }
    }
}
