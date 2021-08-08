package io.webby.url.ws;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.flogger.FluentLogger;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.mu.util.stream.BiCollectors;
import com.google.mu.util.stream.BiStream;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.routekit.ConstToken;
import io.routekit.QueryParseException;
import io.routekit.QueryParser;
import io.routekit.Token;
import io.webby.app.Settings;
import io.webby.url.UrlConfigError;
import io.webby.url.WebsocketAgentConfigError;
import io.webby.url.annotate.ServeWebsocket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.logging.Level;

import static io.webby.url.WebsocketAgentConfigError.failIf;

public class WebsocketAgentBinder {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private WebsocketAgentScanner scanner;
    @Inject private Injector injector;

    public @NotNull Map<String, AgentEndpoint> bindAgents() {
        Set<? extends Class<?>> agentClasses = scanner.getAgentClassesFromClasspath();
        List<AgentBinding> bindings = getBindings(agentClasses);
        return processBindings(bindings, settings.urlParser());
    }

    @VisibleForTesting
    @NotNull List<AgentBinding> getBindings(@NotNull Set<? extends Class<?>> agentClasses) {
        List<AgentBinding> result = new ArrayList<>();
        bindAgents(agentClasses, result::add);
        return result;
    }

    @VisibleForTesting
    void bindAgents(@NotNull Iterable<? extends Class<?>> agentClasses, @NotNull Consumer<AgentBinding> consumer) {
        agentClasses.forEach(klass -> {
            log.at(Level.ALL).log("Processing %s", klass);

            ServeWebsocket serve = getServeAnnotation(klass);
            if (serve.disabled()) {
                log.at(Level.CONFIG).log("Ignoring disabled websocket-agent class: %s", klass);
                return;
            }

            String url = serve.url();
            Class<?> messageClass = serve.messages().length > 0 ? serve.messages()[0] : WebSocketFrame.class;
            boolean acceptsFrame = WebSocketFrame.class.isAssignableFrom(messageClass);

            Multimap<Class<?>, Method> allAcceptors = ArrayListMultimap.create(klass.getDeclaredMethods().length, 3);
            for (Method method : klass.getDeclaredMethods()) {
                if (isMessageAcceptor(method, messageClass)) {
                    Class<?> messageType = method.getParameterTypes()[0];
                    allAcceptors.put(messageType, method);
                }
            }

            Map<Class<?>, Method> acceptors = BiStream.from(allAcceptors.asMap()).mapValues((key, methods) -> {
                assert !methods.isEmpty() : "Internal error: no methods for key %s".formatted(key);

                if (methods.size() > 1) {
                    Method[] pub = filter(methods, Modifier::isPublic);
                    failIf(pub.length > 1, "Multiple public methods match %s: %s".formatted(key, Arrays.toString(pub)));
                    if (pub.length == 1) {
                        return pub[0];
                    }

                    Method[] protect = filter(methods, Modifier::isProtected);
                    failIf(protect.length > 1, "Multiple protected methods match %s: %s".formatted(key, Arrays.toString(protect)));
                    if (protect.length == 1) {
                        return protect[0];
                    }

                    throw new WebsocketAgentConfigError("Multiple methods match %s: %s".formatted(key, methods));
                }
                return Iterables.getFirst(methods, null);
            }).toMap();

            consumer.accept(new AgentBinding(url, klass, messageClass, acceptors, acceptsFrame));
        });
    }

    @VisibleForTesting
    @NotNull Map<String, AgentEndpoint> processBindings(@NotNull Collection<AgentBinding> bindings,
                                                        @NotNull QueryParser parser) {
        return BiStream.from(bindings, binding -> {
            String url = binding.url();
            try {
                List<Token> tokens = parser.parse(url);
                boolean isValidUrl = tokens.size() == 1 && tokens.get(0) instanceof ConstToken;
                failIf(!isValidUrl, "Websocket URL can't contain variables: %s".formatted(url));
            } catch (QueryParseException e) {
                throw new UrlConfigError("Invalid URL: %s".formatted(url), e);
            }
            return url;
        }, binding -> {
            try {
                Object instance = injector.getInstance(binding.agentClass());
                return new AgentEndpoint(instance, binding.acceptors(), binding.acceptsFrame());
            } catch (ConfigurationException e) {
                String message =
                        "Websocket agent instance of %s can't be found or created (use @Inject to register a constructor)"
                        .formatted(binding.agentClass());
                throw new WebsocketAgentConfigError(message, e);
            }
        }).collect(BiCollectors.toMap((endpoint1, endpoint2) -> {
            String message = "Websocket URL duplicated: %s and %s".formatted(endpoint1.instance(), endpoint2.instance());
            throw new WebsocketAgentConfigError(message);
        }));
    }

    @VisibleForTesting
    static @NotNull ServeWebsocket getServeAnnotation(@NotNull AnnotatedElement elem) {
        if (elem.isAnnotationPresent(ServeWebsocket.class)) {
            ServeWebsocket serve = elem.getAnnotation(ServeWebsocket.class);
            failIf(serve.messages().length > 1, "@ServeWebsocket can't have several messages classes: %s".formatted(elem));
            return serve;
        }
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    static boolean isMessageAcceptor(@NotNull Method method, @NotNull Class<?> messageClass) {
        return method.getParameterCount() == 1 && messageClass.isAssignableFrom(method.getParameterTypes()[0]);
    }

    private static @NotNull Method[] filter(@NotNull Collection<Method> methods, @NotNull IntPredicate predicate) {
        return methods.stream().filter(method -> predicate.test(method.getModifiers())).toArray(Method[]::new);
    }
}
