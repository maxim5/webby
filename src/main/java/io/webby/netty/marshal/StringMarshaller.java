package io.webby.netty.marshal;

import com.google.common.io.CharStreams;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import static io.webby.util.EasyCast.castAny;

public class StringMarshaller implements Marshaller {
    @Override
    public void writeChars(@NotNull Writer writer, @NotNull Object instance) throws IOException {
        writer.write(instance.toString());
    }

    @Override
    public <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) throws IOException {
        if (klass.isAssignableFrom(String.class)) {
            return castAny(CharStreams.toString(reader));
        }
        throw new UnsupportedOperationException("Reading an instance of class %s is not supported".formatted(klass));
    }

    @Override
    public <T> @NotNull T readByteBuf(@NotNull ByteBuf byteBuf, @NotNull Class<T> klass, @NotNull Charset charset) {
        if (klass.isAssignableFrom(String.class)) {
            return castAny(byteBuf.toString(charset));
        }
        throw new UnsupportedOperationException("Reading an instance of class %s is not supported".formatted(klass));
    }
}
