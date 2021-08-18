package io.webby.netty.marshal;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.webby.app.AppConfigException;
import io.webby.common.InjectorHelper;
import io.webby.url.annotate.Marshal;
import io.webby.util.EasyClasspath;
import org.jetbrains.annotations.NotNull;

public class MarshallerFactory implements Provider<Json> {
    private final Class<? extends Json> jsonMarshallerClass;
    @Inject private InjectorHelper helper;

    public MarshallerFactory() {
        if (EasyClasspath.isInClassPath("com.google.gson.Gson")) {
            jsonMarshallerClass = GsonMarshaller.class;
        } else if (EasyClasspath.isInClassPath("com.fasterxml.jackson.databind.ObjectMapper")) {
            jsonMarshallerClass = JacksonMarshaller.class;
        } else if (EasyClasspath.isInClassPath("com.alibaba.fastjson.JSON")) {
            jsonMarshallerClass = FastJsonMarshaller.class;
        } else {
            throw new AppConfigException("No JSON library found in classpath. Scanned: Gson, Jackson, Fastjson");
        }
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
