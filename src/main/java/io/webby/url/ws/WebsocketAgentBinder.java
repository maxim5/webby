package io.webby.url.ws;

import com.google.common.collect.*;
import com.google.common.flogger.FluentLogger;
import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.mu.util.stream.BiCollectors;
import com.google.mu.util.stream.BiStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.routekit.ConstToken;
import io.routekit.QueryParseException;
import io.routekit.QueryParser;
import io.routekit.Token;
import io.webby.app.Settings;
import io.webby.netty.marshal.Marshaller;
import io.webby.netty.marshal.MarshallerFactory;
import io.webby.url.UrlConfigError;
import io.webby.url.WebsocketAgentConfigError;
import io.webby.url.annotate.Marshal;
import io.webby.url.annotate.ServeWebsocket;
import io.webby.url.annotate.WsApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.logging.Level;

import static io.webby.url.WebsocketAgentConfigError.failIf;

public class WebsocketAgentBinder {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final int MAX_ID_SIZE = 64;

    @Inject private Settings settings;
    @Inject private WebsocketAgentScanner scanner;
    @Inject private MarshallerFactory marshallers;
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
        String defaultApiVersion = "1";  // TODO: settings.getApiVersion();

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

                WsApi api = getApi(method, idFromName(method.getName()), defaultApiVersion);
                String id = api.id();
                String version = api.version();
                failIf(!id.matches("^[^:/ ]+$"),
                        "API id can't contain any of \":/ \" characters: %s".formatted(id));
                failIf(id.length() > MAX_ID_SIZE,
                        "API id can't be longer than %d: %s".formatted(MAX_ID_SIZE, id));

                ByteBuf bufferId = Unpooled.copiedBuffer(id, settings.charset());
                return new Acceptor(bufferId, version, type, method, acceptsFrame);
            }).toList();

            Field senderField = Arrays.stream(klass.getDeclaredFields())
                    .filter(WebsocketAgentBinder::isSenderField)
                    .peek(field -> field.setAccessible(true))
                    .findAny()
                    .orElse(null);

            consumer.accept(new AgentBinding(url, klass, messageClass, acceptors, senderField, acceptsFrame));
        });
    }

    // TODO: print bindings to the log
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
                Sender sender = binding.sender() != null ? (Sender) binding.sender().get(instance) : null;

                List<Acceptor> acceptors = binding.acceptors();
                if (binding.acceptsFrame()) {
                    ImmutableMap<Class<?>, Acceptor> acceptorsByClass = Maps.uniqueIndex(acceptors, Acceptor::type);
                    return new ClassBasedAgentEndpoint(instance, acceptorsByClass, sender);
                } else {
                    // TODO: settings to annotations
                    FrameType frameType = FrameType.valueOf(settings.getProperty("temp.ws.protocol", "TEXT"));
                    Marshal marshal = settings.defaultResponseContentMarshal();
                    Charset charset = settings.charset();

                    ImmutableMap<ByteBuf, Acceptor> acceptorsById = Maps.uniqueIndex(acceptors, Acceptor::id);
                    Marshaller marshaller = marshallers.getMarshaller(marshal);
                    FrameMetadata metaReader = switch (frameType) {
                        case TEXT -> new TextSeparatorFrameMetadata((byte) ' ', MAX_ID_SIZE);
                        case BINARY -> new BinarySeparatorFrameMetadata((byte) ' ', MAX_ID_SIZE);
                    };
                    FrameConverter<Object> converter = new AcceptorsAwareFrameConverter(marshaller, metaReader, acceptorsById, frameType, charset);
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

    @VisibleForTesting
    static WsApi getApi(@NotNull AnnotatedElement element, @NotNull String defaultId, @NotNull String defaultVersion) {
        if (element.isAnnotationPresent(WsApi.class)) {
            return element.getAnnotation(WsApi.class);
        }
        return new WsApi() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return WsApi.class;
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
            return name.substring(2).toLowerCase();
        }
        return name;
    }

    private static boolean isSenderField(@NotNull Field field) {
        return Sender.class.isAssignableFrom(field.getType());
    }
}
