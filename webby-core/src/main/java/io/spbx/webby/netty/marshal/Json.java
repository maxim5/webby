package io.spbx.webby.netty.marshal;

import com.google.common.io.CharStreams;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public interface Json extends Marshaller {
    @Override
    default void writeChars(@NotNull Writer writer, @NotNull Object instance) throws IOException {
        byte[] bytes = writeBytes(instance);
        writer.write(new String(bytes, charset()));
    }

    @Override
    default <T> @NotNull T readChars(@NotNull Reader reader, @NotNull Class<T> klass) throws IOException {
        String json = CharStreams.toString(reader);
        return readBytes(json.getBytes(charset()), klass);
    }
}
