package io.webby.netty.marshal;

import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.TextFormat;
import io.webby.util.Rethrow;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.UnknownServiceException;
import java.nio.charset.Charset;

import static io.webby.util.EasyCast.castAny;

public class ProtobufMarshaller implements Marshaller {
    @Override
    public void writeChars(@NotNull Writer writer, @NotNull Object instance) throws IOException {
        if (instance instanceof Message message) {
            writer.write(message.toString());
        }
        throw new UnknownServiceException("Protobuf is not available for non-Message classes: %s".formatted(instance));
    }

    @Override
    public void writeBytes(@NotNull OutputStream output, @NotNull Object instance, @NotNull Charset charset) throws IOException {
        if (instance instanceof Message message) {
            message.writeTo(output);
        }
        throw new UnknownServiceException("Protobuf is not available for non-Message classes: %s".formatted(instance));
    }

    @Override
    public byte @NotNull [] writeBytes(@NotNull Object instance, @NotNull Charset charset) throws IOException {
        if (instance instanceof Message message) {
            return message.toByteArray();
        }
        throw new UnknownServiceException("Protobuf is not available for non-Message classes: %s".formatted(instance));
    }

    @Override
    public <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) throws IOException {
        if (Message.class.isAssignableFrom(klass)) {
            Message.Builder builder = getDefaultInstance(castAny(klass)).newBuilderForType();
            TextFormat.merge(reader, builder);
            return castAny(builder.build());
        }
        throw new UnknownServiceException("Protobuf is not available for non-Message classes: %s".formatted(klass));
    }

    @Override
    public <T> @NotNull T readBytes(@NotNull InputStream input, @NotNull Class<T> klass, @NotNull Charset charset) throws IOException {
        if (Message.class.isAssignableFrom(klass)) {
            return getParser(klass).parseFrom(input);
        }
        throw new UnknownServiceException("Protobuf is not available for non-Message classes: %s".formatted(klass));
    }

    @Override
    public <T> @NotNull T readBytes(byte @NotNull [] bytes, @NotNull Class<T> klass, @NotNull Charset charset) throws IOException {
        if (Message.class.isAssignableFrom(klass)) {
            return getParser(klass).parseFrom(bytes);
        }
        throw new UnknownServiceException("Protobuf is not available for non-Message classes: %s".formatted(klass));
    }

    private static <T> @NotNull Parser<T> getParser(@NotNull Class<T> klass) {
        try {
            return castAny(klass.getMethod("parser").invoke(null));
        } catch (Exception e) {
            return Rethrow.rethrow(e);
        }
    }

    public static <T extends Message> T getDefaultInstance(@NotNull Class<T> klass) {
        try {
            return castAny(klass.getMethod("getDefaultInstance").invoke(null));
        } catch (Exception e) {
            return Rethrow.rethrow(e);
        }
    }
}
