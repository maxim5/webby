package io.webby.netty.marshal;

import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.webby.app.AppConfigException;
import io.webby.app.Settings;
import io.webby.common.InjectorHelper;
import io.webby.url.annotate.Marshal;
import io.webby.util.EasyClasspath;
import io.webby.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class MarshallerFactory implements Provider<Json> {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();
    private static final ImmutableMap<String, Pair<String, Class<? extends Json>>> SUPPORTED_JSON = ImmutableMap.of(
        "google:gson", Pair.of("com.google.gson.Gson", GsonMarshaller.class),
        "fasterxml:jackson-databind", Pair.of("com.fasterxml.jackson.databind.ObjectMapper", JacksonMarshaller.class),
        "alibaba:fastjson", Pair.of("com.alibaba.fastjson.JSON", FastJsonMarshaller.class),
        "squareup:moshi", Pair.of("com.squareup.moshi.Moshi", MoshiMarshaller.class),
        "dslplatform:dsl-json", Pair.of("com.dslplatform.json.DslJson", DslJsonMarshaller.class)
    );

    private final Class<? extends Json> jsonMarshallerClass;
    @Inject private InjectorHelper helper;

    @Inject
    public MarshallerFactory(@NotNull Settings settings) {
        jsonMarshallerClass = pickJsonMarshaller(settings);
    }

    @NotNull
    private static Class<? extends Json> pickJsonMarshaller(@NotNull Settings settings) {
        String property = settings.getProperty("json.library");
        Pair<String, Class<? extends Json>> pair = SUPPORTED_JSON.get(property);
        if (property != null && pair == null) {
            log.at(Level.CONFIG).log("Unrecognized json library. Available: %s".formatted(SUPPORTED_JSON.keySet()));
        }
        if (pair != null) {
            if (!EasyClasspath.isInClassPath(pair.first())) {
                log.at(Level.WARNING).log("Json library not found in classpath: %s".formatted(pair.first()));
            }
            return pair.second();
        }

        for (Pair<String, Class<? extends Json>> val : SUPPORTED_JSON.values()) {
            if (EasyClasspath.isInClassPath(val.first())) {
                log.at(Level.INFO).log("Using Json library: %s".formatted(val.first()));
                return val.second();
            }
        }
        throw new AppConfigException("No JSON library found in classpath. Scanned: %s".formatted(SUPPORTED_JSON.keySet()));
    }

    public @NotNull Marshaller getMarshaller(@NotNull Marshal marshal) {
        return switch (marshal) {
            case JSON -> getJson();
            case AS_STRING -> helper.lazySingleton(StringMarshaller.class);
            case PROTOBUF_BINARY -> helper.lazySingleton(ProtobufMarshaller.class);
            case PROTOBUF_JSON -> throw new UnsupportedOperationException();
        };
    }

    public @NotNull Json getJson() {
        return helper.lazySingleton(jsonMarshallerClass);
    }

    @Override
    public Json get() {
        return getJson();
    }
}
