package io.webby.url.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.LazyArgs;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.routekit.*;
import io.webby.app.AppSettings;
import io.webby.netty.StaticServing;
import io.webby.url.*;
import io.webby.url.caller.Caller;
import io.webby.url.caller.CallerFactory;
import io.webby.url.validate.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UrlBinder {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final Pattern PARAM_PATTERN = Pattern.compile("param_([a-zA-Z0-9_$]+)");

    @Inject private AppSettings settings;
    @Inject private HandlerFinder finder;
    @Inject private Injector injector;
    @Inject private CallerFactory callerFactory;
    @Inject private StaticServing staticServing;

    public Router<RouteEndpoint> bindRouter() {
        ArrayList<Binding> bindings = bindHandlersFromClasspath();
        RouterSetup<RouteEndpoint> setup = getRouterSetup(bindings);
        return setup.build();
    }

    @NotNull
    private RouterSetup<RouteEndpoint> getRouterSetup(ArrayList<Binding> bindings) {
        RouterSetup<RouteEndpoint> setup = new RouterSetup<>();
        SimpleQueryParser parser = settings.urlParser();

        {
            Stopwatch stopwatch = Stopwatch.createStarted();
            processBindings(bindings, parser, (url, endpoint) -> {
                setup.add(url, endpoint);
                log.at(Level.FINEST).log("Rule: %s -> %s", url, LazyArgs.lazy(endpoint::describe));
            });
            log.at(Level.INFO).log("Handlers processed in %d ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        }

        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            staticServing.iterateStaticFiles(path -> {
                String url = "/%s".formatted(path);
                setup.add(url, new StaticRouteEndpoint(path, staticServing));
                log.at(Level.FINEST).log("Rule: %s -> %s", url, path);
            });
            log.at(Level.INFO).log("Static files processed in %d ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        } catch (IOException e) {
            throw new UrlConfigError("Failed to add static files to URL router", e);
        }

        return setup.withParser(parser);
    }

    @NotNull
    private ArrayList<Binding> bindHandlersFromClasspath() {
        ArrayList<Binding> bindings = new ArrayList<>();
        try {
            finder.getHandlerClasses().forEach(klass -> {
                Serve serve = klass.getAnnotation(Serve.class);
                SerializeMethod defaultIn = serve.defaultIn();
                SerializeMethod defaultOut = serve.defaultOut();

                for (Method method : klass.getDeclaredMethods()) {
                    Consumer<Binding> sink = binding -> {
                        method.setAccessible(true);
                        bindings.add(binding);
                    };

                    if (method.isAnnotationPresent(GET.class)) {
                        GET ann = method.getAnnotation(GET.class);
                        sink.accept(getBinding("GET", ann.url(), ann.contentType(), null, defaultOut, method));
                    }
                    if (method.isAnnotationPresent(POST.class)) {
                        POST ann = method.getAnnotation(POST.class);
                        sink.accept(getBinding("POST", ann.url(), ann.contentType(), defaultIn, defaultOut, method));
                    }
                    if (method.isAnnotationPresent(PUT.class)) {
                        PUT ann = method.getAnnotation(PUT.class);
                        sink.accept(getBinding("PUT", ann.url(), ann.contentType(), defaultIn, defaultOut, method));
                    }
                    if (method.isAnnotationPresent(DELETE.class)) {
                        DELETE ann = method.getAnnotation(DELETE.class);
                        sink.accept(getBinding("DELETE", ann.url(), ann.contentType(), defaultIn, defaultOut, method));
                    }
                    if (method.isAnnotationPresent(Call.class)) {
                        Call ann = method.getAnnotation(Call.class);
                        for (String type : ann.methods()) {
                            if (!type.equals(type.toUpperCase())) {
                                log.at(Level.WARNING).log(
                                        "@Call http method is not upper-case: %s (at %s)".formatted(type, method));
                            }
                            sink.accept(getBinding(type, ann.url(), ann.contentType(), defaultIn, defaultOut, method));
                        }
                    }
                }
            });
        } catch (Exception e) {
            throw new UrlConfigError(e);
        }
        return bindings;
    }

    @NotNull
    private static Binding getBinding(String type, String url, String contentType,
                                      SerializeMethod in, SerializeMethod out,
                                      Method method) {
        EndpointOptions options = new EndpointOptions(contentType, in, out);
        return new Binding(url, method, type, options);
    }

    @VisibleForTesting
    void processBindings(@NotNull Collection<Binding> bindings,
                         @NotNull SimpleQueryParser parser,
                         @NotNull BiConsumer<String, RouteEndpoint> consumer) {
        try {
            bindings.stream().collect(Collectors.groupingBy(Binding::url)).values().forEach(group -> {
                List<SingleRouteEndpoint> endpoints = group.stream().map(binding -> {
                    Class<?> klass = binding.method().getDeclaringClass();
                    Object instance = injector.getInstance(klass);
                    Map<String, Validator> validators = extractValidators(klass, instance);  // TODO: cache

                    List<String> vars = parser.parse(binding.url())
                            .stream()
                            .map(token -> token instanceof Variable var ? var.name() : null)
                            .filter(Objects::nonNull)
                            .toList();

                    Caller caller = callerFactory.create(instance, binding, validators, vars);
                    return SingleRouteEndpoint.fromBinding(binding, caller);
                }).toList();

                String url = group.stream().map(Binding::url).findFirst().orElseThrow();
                RouteEndpoint endpoint = endpoints.size() == 1 ? endpoints.get(0) : MultiRouteEndpoint.fromEndpoints(endpoints);
                consumer.accept(url, endpoint);
            });
        } catch (ConfigurationException e) {
            throw new UrlConfigError("Handler instance can't be found or created (use @Inject to register a constructor)", e);
        } catch (QueryParseException e) {
            throw new UrlConfigError(e);
        }
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
