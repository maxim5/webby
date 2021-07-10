package io.webby.url;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.flogger.FluentLogger;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.routekit.*;
import io.webby.url.caller.Caller;
import io.webby.url.caller.CallerFactory;
import io.webby.url.validate.Validator;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlBinder {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final Pattern PARAM_PATTERN = Pattern.compile("param_([a-zA-Z0-9_$]+)");

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

    @VisibleForTesting
    Router<Caller> buildRouter(@NotNull Collection<Binding> bindings) {
        SimpleQueryParser parser = SimpleQueryParser.DEFAULT;
        RouterSetup<Caller> setup = new RouterSetup<>();
        try {
            bindings.forEach(binding -> {
                Class<?> klass = binding.method().getDeclaringClass();
                Object instance = injector.getInstance(klass);
                Map<String, Validator> validators = extractValidators(klass, instance);  // TODO: cache

                List<String> vars = parser.parse(binding.url())
                        .stream()
                        .map(token -> token instanceof Variable var ? var.name() : null)
                        .filter(Objects::nonNull)
                        .toList();

                Caller caller = callerFactory.create(instance, binding, validators, vars);
                setup.add(binding.url(), caller);
            });
        } catch (ConfigurationException e) {
            throw new UrlConfigError("Handler instance not found (use @Inject to register a constructor)", e);
        } catch (QueryParseException e) {
            throw new UrlConfigError(e);
        }
        return setup.withParser(parser).build();
    }

    @VisibleForTesting
    static Map<String, Validator> extractValidators(Class<?> klass, Object instance) {
        HashMap<String, Validator> map = new HashMap<>();
        for (Field field : klass.getDeclaredFields()) {
            String fieldName = field.getName();
            Matcher matcher = PARAM_PATTERN.matcher(fieldName);
            if (!matcher.matches()) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(instance);
                if (value instanceof Validator validator) {
                    map.put(matcher.group(1), validator);
                } else {
                    log.at(Level.WARNING).log(
                            "Field %s.%s is not used as validator/converter because is not a Validator instance " +
                            "(rename to stop seeing this warning)",
                            instance, fieldName);
                }
            } catch (IllegalAccessException e) {
                log.at(Level.WARNING).log(
                        "Field %s.%s can't be accessed (rename to stop seeing this warning)",
                        instance, fieldName);
            }
        }
        return map;
    }
}
