package io.webby.netty.marshal;

import com.google.inject.Inject;
import io.webby.common.InjectorHelper;
import io.webby.url.annotate.Marshal;
import org.jetbrains.annotations.NotNull;

public class MarshallerFactory {
    @Inject private InjectorHelper helper;

    public @NotNull Marshaller getMarshaller(@NotNull Marshal marshal) {
        return switch (marshal) {
            case AS_STRING -> helper.lazySingleton(StringMarshaller.class);
            case JSON -> helper.lazySingleton(JsonMarshaller.class);
            case PROTOBUF_BINARY -> helper.lazySingleton(ProtobufMarshaller.class);
            case PROTOBUF_JSON -> throw new UnsupportedOperationException();
        };
    }
}
