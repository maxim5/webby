package io.webby.url.impl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.LazyArgs;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.routekit.*;
import io.webby.app.Settings;
import io.webby.netty.response.StaticServing;
import io.webby.url.*;
import io.webby.url.annotate.*;
import io.webby.url.caller.Caller;
import io.webby.url.caller.CallerFactory;
import io.webby.url.validate.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UrlBinder {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final Pattern PARAM_PATTERN = Pattern.compile("param_([a-zA-Z0-9_$]+)");

    @Inject private Settings settings;
    @Inject private HandlerFinder finder;
    @Inject private Injector injector;
    @Inject private CallerFactory callerFactory;
    @Inject private StaticServing staticServing;

    private final Map<Class<?>, EndpointContext> contextCache = new HashMap<>();

    public Router<RouteEndpoint> bindRouter() {
        Set<? extends Class<?>> handlerClasses = finder.getHandlerClassesFromClasspath();
        ArrayList<Binding> bindings = bindHandlers(handlerClasses);
        RouterSetup<RouteEndpoint> setup = getRouterSetup(bindings);
        return setup.build();
    }

    @NotNull
    private RouterSetup<RouteEndpoint> getRouterSetup(ArrayList<Binding> bindings) {
        RouterSetup<RouteEndpoint> setup = new RouterSetup<>();
        QueryParser parser = settings.urlParser();

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

    @VisibleForTesting
    @NotNull
    ArrayList<Binding> bindHandlers(@NotNull Iterable<? extends Class<?>> handlerClasses) {
        ArrayList<Binding> bindings = new ArrayList<>();
        try {
            Marshal defaultIn = settings.defaultRequestContentMarshal();
            Marshal defaultOut = settings.defaultResponseContentMarshal();

            handlerClasses.forEach(klass -> {
                String classUrl = (klass.isAnnotationPresent(Serve.class)) ? klass.getAnnotation(Serve.class).url() : "";
                Marshal classIn = getMarshalFromAnnotations(klass, defaultIn);
                Marshal classOut = getMarshalFromAnnotations(klass, defaultOut);

                for (Method method : klass.getDeclaredMethods()) {
                    Marshal in = method.getParameterCount() > 0 ?
                            getMarshalFromAnnotations(method.getParameters()[method.getParameterCount() - 1], classIn) :
                            null;
                    Marshal out = getMarshalFromAnnotations(method, classOut);

                    interface Sink {
                        void accept(String type, String url, String contentType, boolean usuallyExpectsContent);
                    }

                    Sink sink = (type, url, contentType, usuallyExpectsContent) -> {
                        if (url.isEmpty()) {
                            UrlConfigError.failIf(classUrl.isEmpty(),
                                    "URL is not set neither in class %s nor in method %s".formatted(klass, method));
                            url = classUrl;
                        }

                        method.setAccessible(true);

                        boolean expectsContent = usuallyExpectsContent && in != null;
                        EndpointOptions options = new EndpointOptions(in, out, contentType, expectsContent);
                        Binding binding = new Binding(url, method, type, options);
                        bindings.add(binding);
                    };

                    if (method.isAnnotationPresent(GET.class)) {
                        GET ann = method.getAnnotation(GET.class);
                        sink.accept("GET", ann.url(), ann.contentType(), false);
                    }
                    if (method.isAnnotationPresent(POST.class)) {
                        POST ann = method.getAnnotation(POST.class);
                        sink.accept("POST", ann.url(), ann.contentType(), true);
                    }
                    if (method.isAnnotationPresent(PUT.class)) {
                        PUT ann = method.getAnnotation(PUT.class);
                        sink.accept("PUT", ann.url(), ann.contentType(), true);
                    }
                    if (method.isAnnotationPresent(DELETE.class)) {
                        DELETE ann = method.getAnnotation(DELETE.class);
                        sink.accept("DELETE", ann.url(), ann.contentType(), false);
                    }
                    if (method.isAnnotationPresent(Call.class)) {
                        Call ann = method.getAnnotation(Call.class);
                        for (String type : ann.methods()) {
                            if (!type.equals(type.toUpperCase())) {
                                log.at(Level.WARNING).log(
                                        "@Call http method is not upper-case: %s (at %s)".formatted(type, method));
                            }
                            sink.accept(type, ann.url(), ann.contentType(), true);
                        }
                    }
                }
            });
        } catch (Exception e) {
            throw (e instanceof UrlConfigError configError ? configError : new UrlConfigError(e));
        }
        return bindings;
    }

    @VisibleForTesting
    void processBindings(@NotNull Collection<Binding> bindings,
                         @NotNull QueryParser parser,
                         @NotNull BiConsumer<String, RouteEndpoint> consumer) {
        try {
            bindings.stream().collect(Collectors.groupingBy(Binding::url)).values().forEach(group -> {
                List<SingleRouteEndpoint> endpoints = group.stream().map(binding -> {
                    Class<?> klass = binding.method().getDeclaringClass();
                    Object instance = injector.getInstance(klass);
                    EndpointContext context = contextCache.computeIfAbsent(klass,
                            key -> new EndpointContext(extractValidators(klass, instance), false));

                    List<String> vars = parser.parse(binding.url())
                            .stream()
                            .map(token -> token instanceof Variable var ? var.name() : null)
                            .filter(Objects::nonNull)
                            .toList();

                    Caller caller = callerFactory.create(instance, binding, context.validators(), vars);
                    return SingleRouteEndpoint.fromBinding(binding, caller, context);
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

    @VisibleForTesting
    @NotNull
    static Marshal getMarshalFromAnnotations(@NotNull AnnotatedElement elem, @NotNull Marshal def) {
        boolean isJson = elem.isAnnotationPresent(Json.class);
        boolean isProto = elem.isAnnotationPresent(Protobuf.class);
        UrlConfigError.failIf(isProto && isJson, "Incompatible @Json and @Protobuf declaration for %s".formatted(elem));
        return isJson ? Marshal.JSON : (isProto && def != Marshal.PROTOBUF_JSON) ? Marshal.PROTOBUF_BINARY : def;
    }
}
