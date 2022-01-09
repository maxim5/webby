package io.webby.netty.marshal;

import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.TextFormat;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;

import static io.webby.util.base.EasyCast.castAny;
import static io.webby.util.base.Unchecked.rethrow;

public record ProtobufMarshaller(@NotNull Charset charset) implements Marshaller {
    @Inject
    public ProtobufMarshaller(@NotNull Charset charset) {
        this.charset = charset;
    }

    @Override
    public @NotNull Marshaller withCustomCharset(@NotNull Charset charset) {
        return charset == this.charset ? this : new ProtobufMarshaller(charset);
    }

    @Override
    public void writeChars(@NotNull Writer writer, @NotNull Object instance) throws IOException {
        if (instance instanceof Message message) {
            writer.write(message.toString());
        } else {
            throw protoUnavailableFor(instance);
        }
    }

    @Override
    public @NotNull String writeString(@NotNull Object instance) {
        if (instance instanceof Message message) {
            return message.toString();
        } else {
            throw protoUnavailableFor(instance);
        }
    }

    @Override
    public void writeBytes(@NotNull OutputStream output, @NotNull Object instance) throws IOException {
        if (instance instanceof Message message) {
            message.writeTo(output);
        } else {
            throw protoUnavailableFor(instance);
        }
    }

    @Override
    public byte @NotNull [] writeBytes(@NotNull Object instance) {
        if (instance instanceof Message message) {
            return message.toByteArray();
        } else {
            throw protoUnavailableFor(instance);
        }
    }

    @Override
    public <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) throws IOException {
        if (Message.class.isAssignableFrom(klass)) {
            Message.Builder builder = getDefaultInstance(castAny(klass)).newBuilderForType();
            TextFormat.merge(reader, builder);
            return castAny(builder.build());
        }
        throw protoUnavailableFor(klass);
    }

    @Override
    public <T> @NotNull T readBytes(@NotNull InputStream input, @NotNull Class<T> klass) throws IOException {
        if (Message.class.isAssignableFrom(klass)) {
            return getParser(klass).parseFrom(input);
        }
        throw protoUnavailableFor(klass);
    }

    @Override
    public <T> @NotNull T readBytes(byte @NotNull [] bytes, @NotNull Class<T> klass) {
        if (Message.class.isAssignableFrom(klass)) {
            try {
                return getParser(klass).parseFrom(bytes);
            } catch (InvalidProtocolBufferException e) {
                return rethrow(e);
            }
        }
        throw protoUnavailableFor(klass);
    }

    private static <T> @NotNull Parser<T> getParser(@NotNull Class<T> klass) {
        try {
            return castAny(klass.getMethod("parser").invoke(null));
        } catch (Exception e) {
            return rethrow(e);
        }
    }

    public static <T extends Message> T getDefaultInstance(@NotNull Class<T> klass) {
        try {
            return castAny(klass.getMethod("getDefaultInstance").invoke(null));
        } catch (Exception e) {
            return rethrow(e);
        }
    }

    private static @NotNull UnsupportedOperationException protoUnavailableFor(@NotNull Object obj) {
        Object desc = obj instanceof Class<?> ? obj : "%s (%s)".formatted(obj.getClass(), obj);
        return new UnsupportedOperationException("Protobuf is not available for non-Message class: %s".formatted(desc));
    }
}
