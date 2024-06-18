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
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.netty.marshal.Marshaller;
import io.webby.netty.marshal.MarshallerFactory;
import io.webby.netty.ws.sender.ChannelMessageSender;
import io.webby.netty.ws.sender.ChannelSender;
import io.webby.netty.ws.sender.MessageSender;
import io.webby.netty.ws.sender.Sender;
import io.webby.routekit.ConstToken;
import io.webby.routekit.QueryParseException;
import io.webby.routekit.Token;
import io.webby.url.UrlConfigError;
import io.webby.url.annotate.*;
import io.webby.url.impl.EndpointView;
import io.webby.url.view.Renderer;
import io.webby.url.view.RendererFactory;
import io.webby.ws.WebsocketAgentConfigError;
import io.webby.ws.context.RequestContext;
import io.webby.ws.convert.AcceptorsAwareFrameConverter;
import io.webby.ws.convert.FrameConverter;
import io.webby.ws.convert.OutFrameConverterListener;
import io.webby.ws.meta.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.logging.Level;

import static io.spbx.util.base.EasyCast.castAny;
import static io.webby.ws.WebsocketAgentConfigError.assure;
import static io.webby.ws.WebsocketAgentConfigError.failIf;
import static io.webby.ws.meta.FrameMetadata.MAX_ID_SIZE;

public class WebsocketAgentBinder {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    @Inject private Settings settings;
    @Inject private WebsocketAgentScanner scanner;
    @Inject private RendererFactory rendererFactory;
    @Inject private MarshallerFactory marshallers;
    @Inject private Injector injector;

    public @NotNull Map<String, AgentEndpoint> bindAgents() {
        Set<Class<?>> agentClasses = scanner.getAgentClassesFromClasspath();
        List<AgentBinding> bindings = getBindings(agentClasses);
        Map<String, AgentEndpoint> result = new HashMap<>();
        processBindings(bindings, (url, endpoint) -> {
            log.at(Level.INFO).log("Registering websocket-agent at %s -> %s", url, endpoint.agent());
            AgentEndpoint existing = result.put(url, endpoint);
            if (existing != null) {
                throw new WebsocketAgentConfigError("Websocket URL %s used by multiple agents: %s and %s",
                                                    url, endpoint.agent(), existing.agent());
            }
        });
        return result;
    }

    @VisibleForTesting
    @NotNull List<AgentBinding> getBindings(@NotNull Set<Class<?>> agentClasses) {
        List<AgentBinding> result = new ArrayList<>();
        bindAgents(agentClasses, result::add);
        return result;
    }

    @VisibleForTesting
    void bindAgents(@NotNull Iterable<Class<?>> agentClasses, @NotNull Consumer<AgentBinding> consumer) {
        FrameType defaultFrameType = settings.defaultFrameType();
        Marshal defaultMarshal = settings.defaultFrameContentMarshal();
        String defaultApiVersion = settings.defaultApiVersion();
        Render defaultRender = settings.defaultRender();

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
            String classUrl = serve.url();
            Render classRender = serve.render().length > 0 ? serve.render()[0] : defaultRender;

            WebsocketProtocol protocol = getProtocolAnnotation(klass);
            Class<?> messageClass = protocol != null ? protocol.messages() : WebSocketFrame.class;
            boolean acceptsFrame = WebSocketFrame.class.isAssignableFrom(messageClass);
            FrameType frameType = getIfPresent(protocol, WebsocketProtocol::type, defaultFrameType);
            Class<? extends FrameMetadata> metaClass = protocol != null ? protocol.meta() : FrameMetadata.class;
            Marshal marshal = getIfPresent(protocol, WebsocketProtocol::marshal, defaultMarshal);

            Multimap<Class<?>, Method> allAcceptors = ArrayListMultimap.create(klass.getDeclaredMethods().length, 3);
            for (Method method : klass.getDeclaredMethods()) {
                if (isMessageAcceptor(method, messageClass) || isMessageContextAcceptor(method, messageClass)) {
                    Class<?> messageType = method.getParameterTypes()[0];
                    allAcceptors.put(messageType, method);
                }
            }
            if (allAcceptors.isEmpty()) {
                log.at(Level.INFO).log("Ignoring empty websocket-agent class: %s", klass);
                return;
            }

            Map<Class<?>, Method> acceptMethodsByType = BiStream.from(allAcceptors.asMap()).mapValues((key, methods) -> {
                assert !methods.isEmpty() : "Internal error: no methods for key %s".formatted(key);

                if (methods.size() > 1) {
                    Method[] pub = filter(methods, Modifier::isPublic);
                    failIf(pub.length > 1, "Multiple public methods match %s: %s", key, Arrays.toString(pub));
                    if (pub.length == 1) {
                        return pub[0];
                    }

                    Method[] protect = filter(methods, Modifier::isProtected);
                    failIf(protect.length > 1, "Multiple protected methods match %s: %s", key, Arrays.toString(protect));
                    if (protect.length == 1) {
                        return protect[0];
                    }

                    throw new WebsocketAgentConfigError("Multiple methods match %s: %s", key, methods);
                }
                return Iterables.getFirst(methods, null);
            }).toMap();

            List<Acceptor> acceptors = acceptMethodsByType.entrySet().stream().map(entry -> {
                Class<?> type = entry.getKey();
                Method method = entry.getValue();

                boolean wantsContext = isMessageContextAcceptor(method, messageClass);
                boolean isVoid = isVoid(method);
                method.setAccessible(true);

                Api api = getApi(method, idFromName(method.getName()), defaultApiVersion);
                String id = api.id();
                String version = api.version();
                failIf(id.isEmpty(), "API id can't be empty: %s", id);
                assure(id.matches("^[a-zA-Z0-9_.-]+$"), "API id contains illegal chars: %s", id);
                assure(id.length() <= MAX_ID_SIZE, "API id can't be longer than %d chars: %s", MAX_ID_SIZE, id);
                assure(version.matches("^[0-9a-z_.-]+$"), "API version contains illegal chars: %s", version);

                EndpointView<?> view = getEndpointViewFromAnnotation(method, classRender);

                ByteBuf bufferId = Unpooled.copiedBuffer(id, settings.charset());
                return new Acceptor(bufferId, version, type, method, view, acceptsFrame, wantsContext, isVoid);
            }).toList();

            Field senderField = Arrays.stream(klass.getDeclaredFields())
                .filter(WebsocketAgentBinder::isSenderField)
                .peek(field -> field.setAccessible(true))
                .findAny()
                .orElse(null);
            if (acceptsFrame && senderField != null && isMessageSenderField(senderField)) {
                throw new WebsocketAgentConfigError(
                    "Agents not defining the message type can't use MessageSender (replace with Sender): %s",
                    senderField.getName());
            }

            consumer.accept(new AgentBinding(classUrl, klass, messageClass, frameType, metaClass, marshal,
                                             acceptors, senderField, acceptsFrame));
        });
    }

    @VisibleForTesting
    void processBindings(@NotNull Collection<AgentBinding> bindings, @NotNull BiConsumer<String, AgentEndpoint> consumer) {
        BiStream.from(bindings, binding -> {
            String url = binding.url();
            try {
                List<Token> tokens = settings.urlParser().parse(url);
                boolean isValidUrl = tokens.size() == 1 && tokens.getFirst() instanceof ConstToken;
                assure(isValidUrl, "Websocket URL can't contain variables: %s", url);
            } catch (QueryParseException e) {
                throw new UrlConfigError("Invalid URL: %s".formatted(url), e);
            }
            return url;
        }, binding -> {
            try {
                Object instance = injector.getInstance(binding.agentClass());
                Sender sender = injectSenderIfNecessary(binding.senderField(), instance);

                List<Acceptor> acceptors = binding.acceptors();
                if (binding.acceptsFrame()) {
                    ImmutableMap<Class<?>, Acceptor> acceptorsByClass = Maps.uniqueIndex(acceptors, Acceptor::type);
                    return new ClassBasedAgentEndpoint(instance, acceptorsByClass, sender);
                } else {
                    FrameType supportedType = binding.frameType();
                    Class<? extends FrameMetadata> metaClass = binding.metaClass();
                    Marshal marshal = binding.marshal();

                    ImmutableMap<ByteBuf, Acceptor> acceptorsById = Maps.uniqueIndex(acceptors, Acceptor::id);
                    Marshaller marshaller = marshallers.getMarshaller(marshal);
                    FrameMetadata metadata = getFrameMetadata(metaClass, acceptorsById.keySet());
                    FrameConverter<Object> converter =
                        new AcceptorsAwareFrameConverter(marshaller, metadata, acceptorsById, supportedType);

                    if (sender instanceof OutFrameConverterListener<?> listener) {
                        listener.onConverter(castAny(converter));
                    }

                    return new FrameConverterEndpoint(instance, converter, sender);
                }
            } catch (ConfigurationException e) {
                String message =
                    "Websocket agent instance of %s can't be found or createdAt (use @Inject to register a constructor)"
                    .formatted(binding.agentClass());
                throw new WebsocketAgentConfigError(message, e);
            } catch (IllegalAccessException e) {
                String message = "Failed to access %s field of the Websocket agent %s"
                    .formatted(binding.senderField(), binding.agentClass());
                throw new WebsocketAgentConfigError(message, e);
            } catch (IllegalArgumentException e) {
                String message = "API identifier is not unique in the Websocket agent %s".formatted(binding.agentClass());
                throw new WebsocketAgentConfigError(message, e);
            }
        }).forEach(consumer);
    }

    private @NotNull FrameMetadata getFrameMetadata(@NotNull Class<? extends FrameMetadata> metaClass,
                                                    @NotNull Set<ByteBuf> acceptorIds) {
        assert !acceptorIds.isEmpty() : "Internal error: acceptor ids must be not empty";
        int minIdLength = acceptorIds.stream().mapToInt(ByteBuf::readableBytes).min().orElseThrow();
        int maxIdLength = acceptorIds.stream().mapToInt(ByteBuf::readableBytes).max().orElseThrow();

        if (metaClass == TextSeparatorFrameMetadata.class) {
            return new TextSeparatorFrameMetadata(
                settings.getByteProperty("ws.metadata.text.separator", TextSeparatorFrameMetadata.DEFAULT_SEPARATOR),
                maxIdLength
            );
        }
        if (metaClass == BinarySeparatorFrameMetadata.class) {
            return new BinarySeparatorFrameMetadata(
                settings.getByteProperty("ws.metadata.binary.separator", BinarySeparatorFrameMetadata.DEFAULT_SEPARATOR),
                maxIdLength
            );
        }
        if (metaClass == BinaryFixedSizeFrameMetadata.class) {
            assure(minIdLength == maxIdLength,
                   "%s requires all API ids to have the same length: %s", metaClass, acceptorIds);
            return new BinaryFixedSizeFrameMetadata(maxIdLength);
        }
        if (metaClass == JsonMetadata.class) {
            return new JsonMetadata(marshallers.getJson(), settings.charset());
        }

        return InjectorHelper.getOrDefault(injector, metaClass, TextSeparatorFrameMetadata::new);
    }

    @VisibleForTesting
    static @NotNull Serve getWebsocketAnnotation(@NotNull AnnotatedElement elem) {
        if (elem.isAnnotationPresent(Serve.class)) {
            Serve serve = elem.getAnnotation(Serve.class);
            failIf(serve.render().length > 1, "@Serve can have only one render: %s", elem);
            return serve;
        }
        throw new UnsupportedOperationException("Internal error: %s does not have @Serve annotation".formatted(elem));
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

    @VisibleForTesting
    static boolean isMessageContextAcceptor(@NotNull Method method, @NotNull Class<?> messageClass) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return parameterTypes.length == 2 &&
               messageClass.isAssignableFrom(parameterTypes[0]) &&
               RequestContext.class.isAssignableFrom(parameterTypes[1]);
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

    private static boolean isSenderField(@NotNull Field field) {
        return Sender.class.isAssignableFrom(field.getType());
    }

    private static boolean isMessageSenderField(@NotNull Field senderField) {
        return MessageSender.class.isAssignableFrom(senderField.getType());
    }

    private static <A, T> T getIfPresent(@Nullable A instance, @NotNull Function<A, T[]> array, @Nullable T def) {
        return instance != null ? getIfPresent(array.apply(instance), def) : def;
    }

    private static <T> T getIfPresent(@NotNull T[] array, @Nullable T def) {
        failIf(array.length > 1, "Multiple array values are not supported: %s", Arrays.toString(array));
        return array.length > 0 ? array[0] : def;
    }

    // The idea of @ImplementedBy from https://stackoverflow.com/questions/4238919/inject-generic-implementation-using-guice
    // works great, but this direct injector feels like a workaround just in case.
    //
    // Maybe make it less hacky via https://github.com/google/guice/wiki/CustomInjections
    private static @Nullable Sender injectSenderIfNecessary(@Nullable Field senderField, @NotNull Object instance)
            throws IllegalAccessException {
        if (senderField == null) {
            return null;
        }
        Object senderFieldValue = senderField.get(instance);
        if (senderFieldValue == null) {
            Sender sender = isMessageSenderField(senderField) ? new ChannelMessageSender<>() : new ChannelSender();
            senderField.set(instance, sender);
            return sender;
        }
        return (Sender) senderFieldValue;
    }
}
