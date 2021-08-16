package io.webby.ws.impl;

import com.google.common.collect.*;
import com.google.common.flogger.FluentLogger;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.mu.util.stream.BiStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.routekit.ConstToken;
import io.routekit.QueryParseException;
import io.routekit.Token;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.netty.marshal.Marshaller;
import io.webby.netty.marshal.MarshallerFactory;
import io.webby.url.UrlConfigError;
import io.webby.url.annotate.*;
import io.webby.ws.Sender;
import io.webby.ws.WebsocketAgentConfigError;
import io.webby.ws.meta.FrameMetadata;
import io.webby.ws.meta.TextSeparatorFrameMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.logging.Level;

import static io.webby.ws.WebsocketAgentConfigError.failIf;
import static io.webby.ws.meta.FrameMetadata.MAX_ID_SIZE;

public class WebsocketAgentBinder {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private WebsocketAgentScanner scanner;
    @Inject private MarshallerFactory marshallers;
    @Inject private Injector injector;
    @Inject private InjectorHelper helper;

    public @NotNull Map<String, AgentEndpoint> bindAgents() {
        Set<? extends Class<?>> agentClasses = scanner.getAgentClassesFromClasspath();
        List<AgentBinding> bindings = getBindings(agentClasses);
        Map<String, AgentEndpoint> result = new HashMap<>();
        processBindings(bindings, (url, endpoint) -> {
            log.at(Level.INFO).log("Registering websocket-agent at %s -> %s", url, endpoint.instance());
            AgentEndpoint existing = result.put(url, endpoint);
            if (existing != null) {
                throw new WebsocketAgentConfigError(
                        "Websocket URL %s used by multiple agents: %s and %s"
                        .formatted(url, endpoint.instance(), existing.instance()));
            }
        });
        return result;
    }

    @VisibleForTesting
    @NotNull List<AgentBinding> getBindings(@NotNull Set<? extends Class<?>> agentClasses) {
        List<AgentBinding> result = new ArrayList<>();
        bindAgents(agentClasses, result::add);
        return result;
    }

    @VisibleForTesting
    void bindAgents(@NotNull Iterable<? extends Class<?>> agentClasses, @NotNull Consumer<AgentBinding> consumer) {
        FrameType defaultFrameType = settings.defaultFrameType();
        Marshal defaultMarshal = settings.defaultFrameContentMarshal();
        String defaultApiVersion = settings.defaultApiVersion();

        agentClasses.forEach(klass -> {
            log.at(Level.ALL).log("Processing %s", klass);

            Serve serve = getWebsocketAnnotation(klass);
            if (!serve.websocket()) {
                return;
            }
            if (serve.disabled()) {
                log.at(Level.CONFIG).log("Ignoring disabled websocket-agent class: %s", klass);
                return;
            }
            String url = serve.url();

            WebsocketProtocol protocol = getProtocolAnnotation(klass);
            Class<?> messageClass = protocol != null ? protocol.messages() : WebSocketFrame.class;
            boolean acceptsFrame = WebSocketFrame.class.isAssignableFrom(messageClass);
            FrameType frameType = getIfPresent(protocol, WebsocketProtocol::type, defaultFrameType);
            Marshal marshal = getIfPresent(protocol, WebsocketProtocol::marshal, defaultMarshal);

            Multimap<Class<?>, Method> allAcceptors = ArrayListMultimap.create(klass.getDeclaredMethods().length, 3);
            for (Method method : klass.getDeclaredMethods()) {
                if (isMessageAcceptor(method, messageClass)) {
                    Class<?> messageType = method.getParameterTypes()[0];
                    allAcceptors.put(messageType, method);
                }
            }

            Map<Class<?>, Method> acceptMethodsByType = BiStream.from(allAcceptors.asMap()).mapValues((key, methods) -> {
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

            List<Acceptor> acceptors = acceptMethodsByType.entrySet().stream().map(entry -> {
                Class<?> type = entry.getKey();
                Method method = entry.getValue();

                Api api = getApi(method, idFromName(method.getName()), defaultApiVersion);
                String id = api.id();
                String version = api.version();
                failIf(!id.matches("^[a-zA-Z0-9_.-]+$"), "API id contains illegal chars: %s".formatted(id));
                failIf(id.length() > MAX_ID_SIZE, "API id can't be longer than %d: %s".formatted(MAX_ID_SIZE, id));
                failIf(!version.matches("^[0-9a-z_.-]+$"), "API version contains illegal chars: %s".formatted(version));

                ByteBuf bufferId = Unpooled.copiedBuffer(id, settings.charset());
                return new Acceptor(bufferId, version, type, method, acceptsFrame);
            }).toList();

            Field senderField = Arrays.stream(klass.getDeclaredFields())
                    .filter(WebsocketAgentBinder::isSenderField)
                    .peek(field -> field.setAccessible(true))
                    .findAny()
                    .orElse(null);

            consumer.accept(new AgentBinding(url, klass, messageClass, frameType, marshal, acceptors, senderField, acceptsFrame));
        });
    }

    @VisibleForTesting
    void processBindings(@NotNull Collection<AgentBinding> bindings, @NotNull BiConsumer<String, AgentEndpoint> consumer) {
        BiStream.from(bindings, binding -> {
            String url = binding.url();
            try {
                List<Token> tokens = settings.urlParser().parse(url);
                boolean isValidUrl = tokens.size() == 1 && tokens.get(0) instanceof ConstToken;
                failIf(!isValidUrl, "Websocket URL can't contain variables: %s".formatted(url));
            } catch (QueryParseException e) {
                throw new UrlConfigError("Invalid URL: %s".formatted(url), e);
            }
            return url;
        }, binding -> {
            try {
                Object instance = injector.getInstance(binding.agentClass());
                Sender sender = binding.sender() != null ? (Sender) binding.sender().get(instance) : null;

                List<Acceptor> acceptors = binding.acceptors();
                if (binding.acceptsFrame()) {
                    ImmutableMap<Class<?>, Acceptor> acceptorsByClass = Maps.uniqueIndex(acceptors, Acceptor::type);
                    return new ClassBasedAgentEndpoint(instance, acceptorsByClass, sender);
                } else {
                    FrameType supportedType = binding.frameType();
                    Marshal marshal = binding.marshal();
                    Charset charset = settings.charset();

                    ImmutableMap<ByteBuf, Acceptor> acceptorsById = Maps.uniqueIndex(acceptors, Acceptor::id);
                    Marshaller marshaller = marshallers.getMarshaller(marshal);
                    FrameMetadata metadata = helper.getOrDefault(FrameMetadata.class, TextSeparatorFrameMetadata::new);
                    FrameConverter<Object> converter =
                            new AcceptorsAwareFrameConverter(marshaller, metadata, acceptorsById, supportedType, charset);
                    return new FrameConverterEndpoint(instance, converter, sender);
                }
            } catch (ConfigurationException e) {
                String message =
                        "Websocket agent instance of %s can't be found or created (use @Inject to register a constructor)"
                        .formatted(binding.agentClass());
                throw new WebsocketAgentConfigError(message, e);
            } catch (IllegalAccessException e) {
                String message = "Failed to access %s field of the Websocket agent %s"
                        .formatted(binding.sender(), binding.agentClass());
                throw new WebsocketAgentConfigError(message, e);
            } catch (IllegalArgumentException e) {
                String message = "API identifier is not unique in the Websocket agent %s".formatted(binding.agentClass());
                throw new WebsocketAgentConfigError(message, e);
            }
        }).forEach(consumer);
    }

    @VisibleForTesting
    static @NotNull Serve getWebsocketAnnotation(@NotNull AnnotatedElement elem) {
        if (elem.isAnnotationPresent(Serve.class)) {
            return elem.getAnnotation(Serve.class);
        }
        throw new UnsupportedOperationException();
    }

    @VisibleForTesting
    static @Nullable WebsocketProtocol getProtocolAnnotation(@NotNull AnnotatedElement elem) {
        if (elem.isAnnotationPresent(WebsocketProtocol.class)) {
            return elem.getAnnotation(WebsocketProtocol.class);
        }
        return null;
    }

    @VisibleForTesting
    static boolean isMessageAcceptor(@NotNull Method method, @NotNull Class<?> messageClass) {
        return method.getParameterCount() == 1 && messageClass.isAssignableFrom(method.getParameterTypes()[0]);
    }

    private static @NotNull Method[] filter(@NotNull Collection<Method> methods, @NotNull IntPredicate predicate) {
        return methods.stream().filter(method -> predicate.test(method.getModifiers())).toArray(Method[]::new);
    }

    @VisibleForTesting
    static Api getApi(@NotNull AnnotatedElement element, @NotNull String defaultId, @NotNull String defaultVersion) {
        if (element.isAnnotationPresent(Api.class)) {
            return element.getAnnotation(Api.class);
        }
        return new Api() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Api.class;
            }

            @Override
            public String id() {
                return defaultId;
            }

            @Override
            public String version() {
                return defaultVersion;
            }
        };
    }

    @VisibleForTesting
    static @NotNull String idFromName(@NotNull String name) {
        if (name.startsWith("on") && name.length() > 2) {
            name = name.substring(2).toLowerCase();
        }
        if (name.length() > MAX_ID_SIZE) {
            name = name.substring(0, MAX_ID_SIZE);
        }
        return name;
    }

    private static boolean isSenderField(@NotNull Field field) {
        return Sender.class.isAssignableFrom(field.getType());
    }

    private static <A, T> T getIfPresent(@Nullable A instance, @NotNull Function<A, T[]> array, @Nullable T def) {
        return instance != null ? getIfPresent(array.apply(instance), def) : def;
    }

    private static <T> T getIfPresent(@NotNull T[] array, @Nullable T def) {
        failIf(array.length > 1, "Multiple array values are not supported: %s".formatted(Arrays.toString(array)));
        return array.length > 0 ? array[0] : def;
    }
}
