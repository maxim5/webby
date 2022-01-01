package io.webby.url.impl;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.LazyArgs;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.routekit.*;
import io.webby.app.AppConfigException;
import io.webby.app.Settings;
import io.webby.netty.response.StaticServing;
import io.webby.netty.response.UserContentServing;
import io.webby.url.HandlerConfigError;
import io.webby.url.UrlConfigError;
import io.webby.url.annotate.*;
import io.webby.url.caller.Caller;
import io.webby.url.caller.CallerFactory;
import io.webby.url.convert.Constraint;
import io.webby.url.view.Renderer;
import io.webby.url.view.RendererFactory;
import io.webby.util.base.TimeIt;
import io.webby.util.collect.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static io.webby.url.HandlerConfigError.failIf;

public class HandlerBinder {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private HandlerScanner scanner;
    @Inject private Injector injector;
    @Inject private CallerFactory callerFactory;
    @Inject private RendererFactory rendererFactory;
    @Inject private StaticServing staticServing;
    @Inject private UserContentServing userContentServing;

    public @NotNull Router<RouteEndpoint> buildHandlerRouter() throws AppConfigException {
        Set<? extends Class<?>> handlerClasses = scanner.getHandlerClassesFromClasspath();
        List<Binding> bindings = getBindings(handlerClasses);
        RouterSetup<RouteEndpoint> setup = getRouterSetup(bindings);
        return setup.build();
    }

    @VisibleForTesting
    @NotNull RouterSetup<RouteEndpoint> getRouterSetup(@NotNull List<Binding> bindings) throws AppConfigException {
        RouterSetup<RouteEndpoint> setup = new RouterSetup<>();
        QueryParser parser = settings.urlParser();

        BiConsumer<String, RouteEndpoint> setupConsumer = (url, endpoint) -> {
            setup.add(url, endpoint);
            log.at(Level.FINE).log("Rule: %s -> %s", url, LazyArgs.lazy(endpoint::describe));
        };

        TimeIt.timeIt(
            () -> processBindings(bindings, parser, setupConsumer),
            millis -> log.at(Level.FINE).log("Endpoints mapped to urls in %d ms", millis)
        );

        String staticFilesUrlPrefix = settings.getProperty("url.static.files.prefix", "/");
        boolean staticFilesDynamicLookup = settings.getBoolProperty("url.static.files.dynamic.lookup", settings.isDevMode());
        try {
            if (staticFilesDynamicLookup) {
                TimeIt.timeItOrDie(
                    () -> staticServing.iterateStaticDirectories(dir -> {
                        String url = UrlFix.joinWithSlash(staticFilesUrlPrefix, dir, "{path}");
                        RouteEndpoint endpoint = new DynamicServingRouteEndpoint(staticFilesUrlPrefix, dir, staticServing);
                        setupConsumer.accept(url, endpoint);
                    }),
                    millis -> log.at(Level.FINE).log("Static files urls processed in %d ms", millis)
                );
            } else {
                TimeIt.timeItOrDie(
                    () -> staticServing.iterateStaticFiles(path -> {
                        String url = UrlFix.joinWithSlash(staticFilesUrlPrefix, path);
                        RouteEndpoint endpoint = new StaticRouteEndpoint(path, staticServing);
                        setupConsumer.accept(url, endpoint);
                    }),
                    millis -> log.at(Level.FINE).log("Static files urls processed in %d ms", millis)
                );
            }
        } catch (IOException e) {
            throw new UrlConfigError("Failed to add static files to URL router", e);
        }

        String userContentUrlPrefix = settings.getProperty("url.user.content.prefix", "/content/");
        String url = UrlFix.joinWithSlash(userContentUrlPrefix, "{*path}");
        RouteEndpoint endpoint = new DynamicServingRouteEndpoint(userContentUrlPrefix, "", userContentServing);
        setupConsumer.accept(url, endpoint);

        return setup.withParser(parser);
    }

    @VisibleForTesting
    @NotNull List<Binding> getBindings(@NotNull Iterable<? extends Class<?>> handlerClasses) {
        try {
            return TimeIt.timeItOrDie(
                () -> {
                    ArrayList<Binding> bindings = new ArrayList<>();
                    bindHandlers(handlerClasses, bindings::add);
                    return bindings;
                },
                (result, millis) -> log.at(Level.FINE).log("Extracted %d bindings in %d ms", result.size(), millis)
            );
        } catch (AppConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new HandlerConfigError("Failed to extract bindings", e);
        }
    }

    @VisibleForTesting
    void bindHandlers(@NotNull Iterable<? extends Class<?>> handlerClasses, @NotNull Consumer<Binding> consumer) {
        Marshal defaultIn = settings.defaultRequestContentMarshal();
        Marshal defaultOut = settings.defaultResponseContentMarshal();
        Render defaultRender = settings.defaultRender();

        handlerClasses.forEach(klass -> {
            log.at(Level.ALL).log("Processing %s", klass);

            Serve serve = getServeAnnotation(klass);
            if (serve.websocket()) {
                return;
            }
            if (serve.disabled()) {
                log.at(Level.CONFIG).log("Ignoring disabled handler class: %s", klass);
                return;
            }
            String classUrl = serve.url();
            Render classRender = serve.render().length > 0 ? serve.render()[0] : defaultRender;
            Marshal classIn = getMarshalFromAnnotations(klass, defaultIn);
            Marshal classOut = getMarshalFromAnnotations(klass, defaultOut);
            EndpointHttp classHttp = getEndpointHttpFromAnnotation(klass);

            for (Method method : klass.getDeclaredMethods()) {
                Marshal in = method.getParameterCount() > 0 ?
                        getMarshalFromAnnotations(method.getParameters()[method.getParameterCount() - 1], classIn) :
                        null;
                Marshal out = getMarshalFromAnnotations(method, classOut);
                EndpointHttp http = getEndpointHttpFromAnnotation(method).mergeWithDefault(classHttp);
                EndpointView<?> view = getEndpointViewFromAnnotation(method, classRender);

                interface Sink {
                    void accept(String type, String url, boolean usuallyExpectsContent);
                }

                Sink sink = (type, url, usuallyExpectsContent) -> {
                    if (url.isEmpty()) {
                        failIf(classUrl.isEmpty(), "URL is not set neither in class %s nor in method %s", klass, method);
                        url = classUrl;
                    }

                    method.setAccessible(true);

                    boolean expectsContent = usuallyExpectsContent && in != null;
                    EndpointOptions options = new EndpointOptions(in, out, http, view, expectsContent);
                    Binding binding = new Binding(url, method, type, options);
                    consumer.accept(binding);
                };

                if (method.isAnnotationPresent(GET.class)) {
                    GET ann = method.getAnnotation(GET.class);
                    sink.accept("GET", ann.url(), false);
                }
                if (method.isAnnotationPresent(POST.class)) {
                    POST ann = method.getAnnotation(POST.class);
                    sink.accept("POST", ann.url(), true);
                }
                if (method.isAnnotationPresent(PUT.class)) {
                    PUT ann = method.getAnnotation(PUT.class);
                    sink.accept("PUT", ann.url(), true);
                }
                if (method.isAnnotationPresent(DELETE.class)) {
                    DELETE ann = method.getAnnotation(DELETE.class);
                    sink.accept("DELETE", ann.url(), false);
                }
                if (method.isAnnotationPresent(Call.class)) {
                    Call ann = method.getAnnotation(Call.class);
                    for (String type : ann.methods()) {
                        if (!type.equals(type.toUpperCase())) {
                            log.at(Level.WARNING).log("@Call http method is not upper-case: %s (at %s)", type, method);
                        }
                        sink.accept(type, ann.url(), true);
                    }
                }
            }
        });
    }

    @VisibleForTesting
    void processBindings(@NotNull Collection<Binding> bindings,
                         @NotNull QueryParser parser,
                         @NotNull BiConsumer<String, RouteEndpoint> consumer) {
        Map<Class<?>, Object> handlersCache = new HashMap<>();  // ensures the same handler instance for all endpoints
        Map<Class<?>, Map<String, Constraint<?>>> constraintsCache = new HashMap<>();  // to reuse expensive calculation
        try {
            bindings.stream().collect(Collectors.groupingBy(Binding::url)).values().forEach(group -> {
                List<SingleRouteEndpoint> endpoints = group.stream().map(binding -> {
                    Method method = binding.method();
                    log.at(Level.ALL).log("Processing %s", method);

                    Class<?> klass = method.getDeclaringClass();
                    Object instance = handlersCache.computeIfAbsent(klass, key -> injector.getInstance(klass));
                    Map<String, Constraint<?>> compute = constraintsCache
                            .computeIfAbsent(klass, key -> extractConstraints(klass, instance));
                    EndpointContext context = new EndpointContext(compute, false, isVoid(method));

                    List<String> vars = parser.parse(binding.url())
                            .stream()
                            .map(token -> token instanceof Variable var ? var.name() : null)
                            .filter(Objects::nonNull)
                            .toList();

                    Caller caller = callerFactory.create(instance, binding, context.constraints(), vars);
                    return SingleRouteEndpoint.fromBinding(binding, caller, context);
                }).toList();

                String url = group.stream().map(Binding::url).findFirst().orElseThrow();
                RouteEndpoint endpoint = endpoints.size() == 1 ? endpoints.get(0) : MultiRouteEndpoint.fromEndpoints(endpoints);
                consumer.accept(url, endpoint);
            });
        } catch (ConfigurationException e) {
            throw new HandlerConfigError("Handler instance can't be found or createdAt (use @Inject to register a constructor)", e);
        } catch (QueryParseException e) {
            throw new UrlConfigError(e);
        }
    }

    @VisibleForTesting
    static @NotNull Map<String, Constraint<?>> extractConstraints(@NotNull Class<?> klass, @NotNull Object instance) {
        Map<String, Constraint<?>> map = new HashMap<>();
        for (Field field : klass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Param.class)) {
                continue;
            }
            String var = field.getAnnotation(Param.class).var();
            String fieldName = field.getName();
            if (var.isEmpty()) {
                var = fieldName;
            }

            field.setAccessible(true);
            try {
                Object value = field.get(instance);
                if (value instanceof Constraint<?> constraint) {
                    failIf(map.containsKey(var), "Duplicate constraints for %s variable", var);
                    map.put(var, constraint);
                    log.at(Level.FINE).log("Recognized a constraint for %s variable", var);
                } else {
                    log.at(Level.WARNING).log(
                            "Field %s.%s is not used as converter/validator because is not a Constraint instance " +
                            "(rename to stop seeing this warning)",
                            instance, fieldName);
                }
            } catch (IllegalAccessException e) {
                log.at(Level.WARNING).log(
                        "Field %s.%s can't be accessed (remove @Param annotation to stop seeing this warning)",
                        instance, fieldName);
            }
        }
        return map;
    }

    @VisibleForTesting
    static @NotNull Serve getServeAnnotation(@NotNull AnnotatedElement elem) {
        if (elem.isAnnotationPresent(Serve.class)) {
            Serve serve = elem.getAnnotation(Serve.class);
            failIf(serve.render().length > 1, "@Serve can have only one render: %s", elem);
            return serve;
        }
        return new Serve() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Serve.class;
            }

            @Override
            public String url() {
                return "";
            }

            @Override
            public boolean websocket() {
                return false;
            }

            @Override
            public Render[] render() {
                return new Render[0];
            }

            @Override
            public boolean disabled() {
                return false;
            }
        };
    }

    @VisibleForTesting
    static @NotNull Marshal getMarshalFromAnnotations(@NotNull AnnotatedElement elem, @NotNull Marshal def) {
        boolean isJson = elem.isAnnotationPresent(Json.class);
        boolean isProto = elem.isAnnotationPresent(Protobuf.class);
        boolean isView = elem.isAnnotationPresent(View.class);
        failIf(isProto && isJson, "Incompatible @Json and @Protobuf declaration for %s", elem);
        failIf(isProto && isView, "Incompatible @View and @Protobuf declaration for %s", elem);
        failIf(isJson && isView, "Incompatible @Json and @View declaration for %s", elem);
        return isJson ? Marshal.JSON : (isProto && def != Marshal.PROTOBUF_JSON) ? Marshal.PROTOBUF_BINARY : def;
    }

    @VisibleForTesting
    static @NotNull EndpointHttp getEndpointHttpFromAnnotation(@NotNull AnnotatedElement element) {
        if (element.isAnnotationPresent(Http.class)) {
            Http http = element.getAnnotation(Http.class);
            String contentType = http.contentType();
            List<Pair<String, String>> headers = Arrays.stream(http.headers())
                    .map(header -> Pair.of(header.name(), header.value()))
                    .toList();
            return new EndpointHttp(contentType, headers);
        }
        return EndpointHttp.EMPTY;
    }

    @VisibleForTesting
    @Nullable EndpointView<?> getEndpointViewFromAnnotation(@NotNull AnnotatedElement element, @NotNull Render render) {
        if (element.isAnnotationPresent(View.class)) {
            View view = element.getAnnotation(View.class);
            String templateName = view.template();
            Renderer<?> renderer = rendererFactory.getRenderer(render, templateName);
            return EndpointView.of(renderer, templateName);
        }
        return null;
    }

    private static boolean isVoid(@NotNull Method method) {
        return method.getReturnType().equals(Void.TYPE);
    }
}
